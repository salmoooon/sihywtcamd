package com.github.galatynf.sihywtcamd.mixin.zombie;

import com.github.galatynf.sihywtcamd.Sihywtcamd;
import com.github.galatynf.sihywtcamd.config.ModConfig;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ZombieEntity.class)
public abstract class ZombieMixin extends HostileEntity {
    @Shadow public abstract void setCanBreakDoors(boolean canBreakDoors);

    @Shadow protected abstract boolean shouldBreakDoors();

    @Shadow public abstract boolean isBaby();

    protected ZombieMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tryAttack", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void stealHealth(Entity target, CallbackInfoReturnable<Boolean> cir, boolean bl) {
        if (bl && ModConfig.get().zombie.general.attackHeal) {
            float damage = (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (target instanceof LivingEntity livingEntity) {
                damage += EnchantmentHelper.getAttackDamage(this.getMainHandStack(), livingEntity.getGroup());
            }
            this.setHealth(Math.min(this.getHealth() + damage, this.getMaxHealth()));
        }
    }

    @Inject(method = "applyAttributeModifiers", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/ZombieEntity;initAttributes()V", shift = At.Shift.AFTER), cancellable = true)
    private void applyDifferentAttributes(float chanceMultiplier, CallbackInfo ci) {
        float random = this.random.nextFloat();
        if (ModConfig.get().zombie.general.attributeVariations && !this.isBaby()) {
            if (random < 0.1f) {
                this.setCustomName(Text.of("Caller"));
                double value = 0.2 + 0.15 * chanceMultiplier + 0.15 * this.random.nextDouble();
                this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).addPersistentModifier(
                        new EntityAttributeModifier("Caller zombie bonus", value, EntityAttributeModifier.Operation.ADDITION));
            } else if (random < 0.3f) {
                this.setCustomName(Text.of("Tank"));
                this.setCanBreakDoors(this.shouldBreakDoors());
                double value = 0.5 + 1.0 * chanceMultiplier + this.random.nextDouble();
                this.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addPersistentModifier(
                        new EntityAttributeModifier("Tank zombie bonus", value, EntityAttributeModifier.Operation.MULTIPLY_BASE));
            } else if (random < 0.5f) {
                this.setCustomName(Text.of("Runner"));
                double value = 0.2 + 0.15 * chanceMultiplier + 0.15 * this.random.nextDouble();
                this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addPersistentModifier(
                        new EntityAttributeModifier("Runner zombie bonus", value, EntityAttributeModifier.Operation.MULTIPLY_BASE));
            } else if (random < 0.7f) {
                this.setCustomName(Text.of("Unstoppable"));
                double value = 0.2 + 0.15 * chanceMultiplier + 0.15 * this.random.nextDouble();
                this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).addPersistentModifier(
                        new EntityAttributeModifier("Unstoppable zombie bonus", value, EntityAttributeModifier.Operation.ADDITION));
            }

            this.setCustomNameVisible(Sihywtcamd.DEBUG);
            ci.cancel();
        }
    }

    @Inject(method = "initAttributes", at = @At("HEAD"), cancellable = true)
    private void initWithIncreasedAttributes(CallbackInfo ci) {
        if (ModConfig.get().zombie.general.moreKnockbackResistance) {
            this.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(0.2 + 0.2 * this.random.nextDouble());
        }
        if (ModConfig.get().zombie.general.spawnMoreReinforcement) {
            this.getAttributeInstance(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(0.2 + 0.2 * this.random.nextDouble());
            ci.cancel();
        }
    }
}
