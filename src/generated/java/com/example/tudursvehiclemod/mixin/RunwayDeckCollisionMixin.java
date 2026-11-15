package com.example.tudursvehiclemod.mixin;

import com.example.tudursvehiclemod.entity.RunwayDeckCollision;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

/** Adds the runways' surfaces (entity.RunwayDeckCollision) to what the game collides entities with. EntityGetter.getEntityCollisions() is where every entity-based collision shape is looked up - movement (Entity.collide()), "is there room here" checks (noCollision(): sneaking at an edge, spawning, standing up) - on both sides.
 * 1.20.1 (Forge): the Mixin version Forge ships can't inject into an interface, and in 1.20.1 getEntityCollisions() is only
 * EntityGetter's default method (neither Level, ServerLevel nor ClientLevel overrides it). So Level gets an override: the
 * interface's 1.20.1 logic (EntityGetter#getEntityCollisions), then the runways added as the Fabric version's injector does. */
@Mixin(Level.class)
public abstract class RunwayDeckCollisionMixin implements EntityGetter {

	@Override
	public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB box) {
		if (box.getSize() < 1.0E-7) {
			return List.of();
		}
		Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
		List<Entity> entities = this.getEntities(entity, box.inflate(1.0E-7), predicate);
		List<VoxelShape> shapes;
		if (entities.isEmpty()) {
			shapes = List.of();
		} else {
			ImmutableList.Builder<VoxelShape> builder = ImmutableList.builderWithExpectedSize(entities.size());
			for (Entity e : entities) {
				builder.add(Shapes.create(e.getBoundingBox()));
			}
			shapes = builder.build();
		}
		return RunwayDeckCollision.withRunways((Level) (Object) this, entity, box, shapes);
	}
}
