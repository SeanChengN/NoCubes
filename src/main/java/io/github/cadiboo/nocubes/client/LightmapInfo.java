package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3.PooledVec3;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * @author Cadiboo
 */
//TODO pooling?
public class LightmapInfo {

	public final int skylight0;

	public LightmapInfo(final int skylight0, final int skylight1, final int skylight2, final int skylight3, final int blocklight0, final int blocklight1, final int blocklight2, final int blocklight3) {
		this.skylight0 = skylight0;
		this.skylight1 = skylight1;
		this.skylight2 = skylight2;
		this.skylight3 = skylight3;
		this.blocklight0 = blocklight0;
		this.blocklight1 = blocklight1;
		this.blocklight2 = blocklight2;
		this.blocklight3 = blocklight3;
	}

	public final int skylight1;
	public final int skylight2;
	public final int skylight3;
	public final int blocklight0;
	public final int blocklight1;
	public final int blocklight2;
	public final int blocklight3;

	public static LightmapInfo generateLightmapInfo(
			final PooledVec3 v0,
			final PooledVec3 v1,
			final PooledVec3 v2,
			final PooledVec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			final int[] pos,
			final IBlockAccess blockAccess, final PooledMutableBlockPos pooledMutableBlockPos
	) {

//		final int px0 = renderChunkPositionX + pos[0];
//		final int py0 = renderChunkPositionY + pos[1];
//		final int pz0 = renderChunkPositionZ + pos[2];
//		final int px1 = px0 + 1;
//		final int py1 = py0 + 1;
//		final int pz1 = pz0 + 1;
//
//		final int[][][] packedLight = new int[][][]{{
//				{
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px1, py0, pz1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px1, py0, pz0)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//				},
//				{
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px1, py1, pz1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px1, py1, pz0)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//				}
//		}, {
//				{
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px0, py0, pz1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px0, py0, pz0)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//				},
//				{
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px0, py1, pz1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//						blockAccess.getBlockState(pooledMutableBlockPos.setPos(px0, py1, pz0)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos),
//				}
//		}};

//		final int v0X = clamp((round(v0.x) - pos[0] - renderChunkPositionX), 0, 1);
//		final int v0Y = clamp((round(v0.y) - pos[1] - renderChunkPositionY), 0, 1);
//		final int v0Z = clamp((round(v0.z) - pos[2] - renderChunkPositionZ), 0, 1);
//		final int v1X = clamp((round(v1.x) - pos[0] - renderChunkPositionX), 0, 1);
//		final int v1Y = clamp((round(v1.y) - pos[1] - renderChunkPositionY), 0, 1);
//		final int v1Z = clamp((round(v1.z) - pos[2] - renderChunkPositionZ), 0, 1);
//		final int v2X = clamp((round(v2.x) - pos[0] - renderChunkPositionX), 0, 1);
//		final int v2Y = clamp((round(v2.y) - pos[1] - renderChunkPositionY), 0, 1);
//		final int v2Z = clamp((round(v2.z) - pos[2] - renderChunkPositionZ), 0, 1);
//		final int v3X = clamp((round(v3.x) - pos[0] - renderChunkPositionX), 0, 1);
//		final int v3Y = clamp((round(v3.y) - pos[1] - renderChunkPositionY), 0, 1);
//		final int v3Z = clamp((round(v3.z) - pos[2] - renderChunkPositionZ), 0, 1);

//		final int v0X = floor(v0.x - pos[0] - renderChunkPositionX);
//		final int v0Y = floor(v0.y - pos[1] - renderChunkPositionY);
//		final int v0Z = floor(v0.z - pos[2] - renderChunkPositionZ);
//		final int v1X = floor(v1.x - pos[0] - renderChunkPositionX);
//		final int v1Y = floor(v1.y - pos[1] - renderChunkPositionY);
//		final int v1Z = floor(v1.z - pos[2] - renderChunkPositionZ);
//		final int v2X = floor(v2.x - pos[0] - renderChunkPositionX);
//		final int v2Y = floor(v2.y - pos[1] - renderChunkPositionY);
//		final int v2Z = floor(v2.z - pos[2] - renderChunkPositionZ);
//		final int v3X = floor(v3.x - pos[0] - renderChunkPositionX);
//		final int v3Y = floor(v3.y - pos[1] - renderChunkPositionY);
//		final int v3Z = floor(v3.z - pos[2] - renderChunkPositionZ);

//		return new LightmapInfo(
//				packedLight[v0X][v0Y][v0Z] >> 16 & 0xFFFF,
//				packedLight[v1X][v1Y][v1Z] >> 16 & 0xFFFF,
//				packedLight[v2X][v2Y][v2Z] >> 16 & 0xFFFF,
//				packedLight[v3X][v3Y][v3Z] >> 16 & 0xFFFF,
//				packedLight[v0X][v0Y][v0Z] & 0xFFFF,
//				packedLight[v1X][v1Y][v1Z] & 0xFFFF,
//				packedLight[v2X][v2Y][v2Z] & 0xFFFF,
//				packedLight[v3X][v3Y][v3Z] & 0xFFFF
//		);

		final Vec3[] vec3s = {v0, v1, v2, v3};

		final int[] packedLight = new int[36];

		// from (-1, -1, -1) to (1, 1, 1) for each vertex
		int index = 0;
		for (int v = 0; v < 4; v++) {
			for (int z = -1; z < 1; z++) {
				for (int y = -1; y < 1; y++) {
					for (int x = -1; x < 1; x++, index++) {
						final Vec3 vec = vec3s[v];
						packedLight[index] = blockAccess.getBlockState(pooledMutableBlockPos.setPos(vec.x + x, vec.y + y, vec.z + z)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
					}
				}
			}
		}

		final int[] skyLight = new int[4];
		final int[] blockLight = new int[4];
		for (int v = 0; v < 4; ++v) {
			index = v * 4;
			if ((packedLight[index] >> 16 & 0xFFFF) != 0) {
				skyLight[v] = packedLight[index] >> 16 & 0xFFFF;
			} else {
				skyLight[v] = max(
						packedLight[index] >> 16 & 0xFFFF,
						packedLight[index + 1] >> 16 & 0xFFFF,
						packedLight[index + 2] >> 16 & 0xFFFF,
						packedLight[index + 3] >> 16 & 0xFFFF,
						packedLight[index + 4] >> 16 & 0xFFFF,
						packedLight[index + 5] >> 16 & 0xFFFF,
						packedLight[index + 6] >> 16 & 0xFFFF,
						packedLight[index + 7] >> 16 & 0xFFFF,
						packedLight[index + 8] >> 16 & 0xFFFF
				);
			}
			if ((packedLight[index] & 0xFFFF) != 0) {
				blockLight[v] = packedLight[index] & 0xFFFF;
			} else {
				blockLight[v] = max(
						packedLight[index] & 0xFFFF,
						packedLight[index + 1] & 0xFFFF,
						packedLight[index + 2] & 0xFFFF,
						packedLight[index + 3] & 0xFFFF,
						packedLight[index + 4] & 0xFFFF,
						packedLight[index + 5] & 0xFFFF,
						packedLight[index + 6] & 0xFFFF,
						packedLight[index + 7] & 0xFFFF,
						packedLight[index + 8] & 0xFFFF
				);
			}
		}

		return new LightmapInfo(
				skyLight[0],
				skyLight[1],
				skyLight[2],
				skyLight[3],
				blockLight[0],
				blockLight[1],
				blockLight[2],
				blockLight[3]
		);

	}

	public static final int max(int... ints) {
		int max = 0;
		for (final int anInt : ints) {
			if (max < anInt) max = anInt;
		}
		return max;
	}

	//FIXME: MAX PRIORITY FOR 0.2.0
	//TODO: FIX THIS SHIT
//	final double pos0X = v0.x + ((v0.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
//	final double pos0Y = v0.y + ((v0.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
//	final double pos0Z = v0.z + (v0.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;
//
//	final int packedLight0 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos0X), floor(pos0Y), floor(pos0Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);
//
//	lightmapSkyLight0 = packedLight0 >> 16 & 0xFFFF;
//	lightmapBlockLight0 = packedLight0 & 0xFFFF;
//
//	final double pos1X = v1.x + ((v1.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
//	final double pos1Y = v1.y + ((v1.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
//	final double pos1Z = v1.z + (v1.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;
//
//	final int packedLight1 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos1X), floor(pos1Y), floor(pos1Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);
//
//	lightmapSkyLight1 = packedLight1 >> 16 & 0xFFFF;
//	lightmapBlockLight1 = packedLight1 & 0xFFFF;
//
//	final double pos2X = v2.x + ((v2.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
//	final double pos2Y = v2.y + ((v2.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
//	final double pos2Z = v2.z + (v2.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;
//
//	final int packedLight2 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos2X), floor(pos2Y), floor(pos2Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);
//
//	lightmapSkyLight2 = packedLight2 >> 16 & 0xFFFF;
//	lightmapBlockLight2 = packedLight2 & 0xFFFF;
//
//	final double pos3X = v3.x + ((v3.x - pos[0] - renderChunkPositionX) * ModConfig.isosurfaceLevel);
//	final double pos3Y = v3.y + ((v3.y - pos[1] - renderChunkPositionY) * ModConfig.isosurfaceLevel);
//	final double pos3Z = v3.z + (v3.z - pos[2] - renderChunkPositionZ) * ModConfig.isosurfaceLevel;
//
//	final int packedLight3 = cache.getBlockState(pooledMutableBlockPos.setPos(floor(pos3X), floor(pos3Y), floor(pos3Z))).getPackedLightmapCoords(cache, pooledMutableBlockPos);
//
//	lightmapSkyLight3 = packedLight3 >> 16 & 0xFFFF;
//	lightmapBlockLight3 = packedLight3 & 0xFFFF;
//
//	//v0 =
//	//TODO: change to ModUtil#map
//	//<editor-fold desc="Snap to integer coords and light index">
//	final int v0X = clamp((round(v0.x) - pos[0] - renderChunkPositionX), 0, 1);
//	final int v0Y = clamp((round(v0.y) - pos[1] - renderChunkPositionY), 0, 1);
//	final int v0Z = clamp((round(v0.z) - pos[2] - renderChunkPositionZ), 0, 1);
//	final int v1X = clamp((round(v1.x) - pos[0] - renderChunkPositionX), 0, 1);
//	final int v1Y = clamp((round(v1.y) - pos[1] - renderChunkPositionY), 0, 1);
//	final int v1Z = clamp((round(v1.z) - pos[2] - renderChunkPositionZ), 0, 1);
//	final int v2X = clamp((round(v2.x) - pos[0] - renderChunkPositionX), 0, 1);
//	final int v2Y = clamp((round(v2.y) - pos[1] - renderChunkPositionY), 0, 1);
//	final int v2Z = clamp((round(v2.z) - pos[2] - renderChunkPositionZ), 0, 1);
//	final int v3X = clamp((round(v3.x) - pos[0] - renderChunkPositionX), 0, 1);
//	final int v3Y = clamp((round(v3.y) - pos[1] - renderChunkPositionY), 0, 1);
//	final int v3Z = clamp((round(v3.z) - pos[2] - renderChunkPositionZ), 0, 1);
//	//</editor-fold>
//
//	//<editor-fold desc="get and unpack lightmap coords">
//	lightmapSkyLight0 = packedLight[v0X][v0Y][v0Z] >> 16 & 0xFFFF;
//	lightmapBlockLight0 = packedLight[v0X][v0Y][v0Z] & 0xFFFF;
//	lightmapSkyLight1 = packedLight[v1X][v1Y][v1Z] >> 16 & 0xFFFF;
//	lightmapBlockLight1 = packedLight[v1X][v1Y][v1Z] & 0xFFFF;
//	lightmapSkyLight2 = packedLight[v2X][v2Y][v2Z] >> 16 & 0xFFFF;
//	lightmapBlockLight2 = packedLight[v2X][v2Y][v2Z] & 0xFFFF;
//	lightmapSkyLight3 = packedLight[v3X][v3Y][v3Z] >> 16 & 0xFFFF;
//	lightmapBlockLight3 = packedLight[v3X][v3Y][v3Z] & 0xFFFF;
//	//</editor-fold>

}