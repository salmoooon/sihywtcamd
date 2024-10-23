package com.github.galatynf.sihywtcamd.mixin;

import com.github.galatynf.sihywtcamd.Sihywtcamd;
import com.github.galatynf.sihywtcamd.cardinal.MyComponents;
import com.github.galatynf.sihywtcamd.config.ModConfig;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SlimeEntity.class)
public abstract class SlimeMixin extends MobEntity {
    @Shadow public abstract int getSize();
    @Shadow public abstract EntityType<? extends SlimeEntity> getType();
    @Shadow protected abstract ParticleEffect getParticles();

    @Shadow public abstract void setSize(int size, boolean heal);

    protected SlimeMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;initialize(Lnet/minecraft/world/ServerWorldAccess;Lnet/minecraft/world/LocalDifficulty;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/EntityData;)Lnet/minecraft/entity/EntityData;"))
    private void spawnBigger(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                             EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        SlimeEntity slime = (SlimeEntity) (Object) this;
        if (ModConfig.get().overworld.slime.biggerSize
                && !(slime instanceof MagmaCubeEntity)
                && this.random.nextFloat() < 0.05f + 0.05f * difficulty.getClampedLocalDifficulty()) {
            this.setSize(8, true);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    protected void onTick(CallbackInfo ci) {
        MyComponents.SLIME_COMPONENT.get(this).updateMerged();

        SlimeEntity thisSlime = (SlimeEntity) (Object) this;
        if (!this.getWorld().isClient()
                && ModConfig.get().overworld.slime.magmaConversion
                && !(thisSlime instanceof MagmaCubeEntity)
                && this.getWorld().getRegistryKey().equals(World.NETHER)
                && this.isInLava()) {
            int size = this.getSize();
            MagmaCubeEntity magmaCube = this.convertTo(EntityType.MAGMA_CUBE, false);
            if (magmaCube != null) {
                magmaCube.setSize(size, true);
                this.playSound(SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.7F, 1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            }
        }
    }

    @Inject(method = "pushAwayFrom", at = @At("TAIL"))
    private void tryToMerge(Entity entity, CallbackInfo ci) {
        if (!this.getType().equals(entity.getType())) return;

        SlimeEntity thisSlime = (SlimeEntity) (Object) this;
        SlimeEntity otherSlime = (SlimeEntity) entity;
        if (!this.getWorld().isClient()
                && !(thisSlime instanceof MagmaCubeEntity)
                && ModConfig.get().overworld.slime.canMerge
                && MyComponents.SLIME_COMPONENT.get(this).canMerge()
                && MyComponents.SLIME_COMPONENT.get(otherSlime).canMerge()
                && this.isAlive()
                && this.getSize() < 4
                && this.getSize() == otherSlime.getSize()) {
            MyComponents.SLIME_COMPONENT.get(this).setMerged();
            otherSlime.discard();
            this.setSize(this.getSize() * 2, true);
            this.getWorld().addParticle(this.getParticles(), this.getX(), this.getY(), this.getZ(),
                    0.0, 0.0, 0.0);
            this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0f,
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);

            if (Sihywtcamd.DEBUG) {
                this.setCustomName(Text.of("Merged"));
                this.setCustomNameVisible(true);
            }
        }
    }

    @ModifyVariable(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/SlimeEntity;setInvulnerable(Z)V"))
    private SlimeEntity transferMergedGene(SlimeEntity slime, RemovalReason value) {
        boolean hasMerged = MyComponents.SLIME_COMPONENT.get(this).hasMerged();
        if (hasMerged) {
            MyComponents.SLIME_COMPONENT.get(slime).setMerged();
        }

        if (Sihywtcamd.DEBUG && hasMerged) {
            this.setCustomName(Text.of("Merged Child"));
            this.setCustomNameVisible(true);
        }

        return slime;
    }

    @Inject(method = "damage", at = @At("HEAD"))
    protected void onDamage(LivingEntity target, CallbackInfo ci) {

    }
}
