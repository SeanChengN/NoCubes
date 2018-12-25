package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;

/**
 * Implementation of the SurfaceNets algorithm in Minecraft
 *
 * @author Cadiboo
 */
public final class SurfaceNets {

	public static final int[] CUBE_EDGES = new int[24];
	public static final int[] EDGE_TABLE = new int[256];

	// because the tables are so big we compute them in a static {} instead of hardcoding them
	//Precompute edge table, like Paul Bourke does.
	// This saves a bit of time when computing the centroid of each boundary cell
	static {

		//Initialize the cube_edges table
		// This is just the vertex number (number of corners) of each cube
		int k = 0;
		for (int i = 0; i < 8; ++i) {
			for (int em = 1; em <= 4; em <<= 1) {
				int j = i ^ em;
				if (i <= j) {
					CUBE_EDGES[k++] = i;
					CUBE_EDGES[k++] = j;
				}
			}
		}

		// nope, I don't understand this either
		// yay, Lookup Tables...
		//Initialize the intersection table.
		//  This is a 2^(cube configuration) ->  2^(edge configuration) map
		//  There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
		for (int i = 0; i < 256; ++i) {
			int em = 0;
			for (int j = 0; j < 24; j += 2) {
				final boolean a = (i & (1 << CUBE_EDGES[j])) != 0;
				final boolean b = (i & (1 << CUBE_EDGES[j + 1])) != 0;
				em |= a != b ? 1 << (j >> 1) : 0;
			}
			EDGE_TABLE[i] = em;
		}

	}

	public static void renderPre(final RebuildChunkPreEvent event) {

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

		ClientUtil.handleTransparentBlocksRenderType(event);

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		final float isosurfaceLevel = ModConfig.getIsosurfaceLevel();
		final BlockPos.MutableBlockPos pos = event.getBlockPos();
		final ChunkCache cache = event.getChunkCache();
		final IBlockState state = event.getBlockState();
		final BlockRendererDispatcher blockRendererDispatcher = event.getBlockRendererDispatcher();

		BlockPos.MutableBlockPos texturePos = pos;
		IBlockState textureState = state;

		final Vec3[] points = new Vec3[]{
				new Vec3(0.0D, 0.0D, 1.0D),
				new Vec3(1.0D, 0.0D, 1.0D),
				new Vec3(1.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 0.0D, 0.0D),
				new Vec3(0.0D, 1.0D, 1.0D),
				new Vec3(1.0D, 1.0D, 1.0D),
				new Vec3(1.0D, 1.0D, 0.0D),
				new Vec3(0.0D, 1.0D, 0.0D)
		};

		// Read in 8 field values around this vertex and store them in an array
		final float[] neighbourDensities = new float[8];
		// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
		int neighbourMask = 0; // called cubeIndex by lots of implementation
		{
			final BlockPos.PooledMutableBlockPos mutablePos = BlockPos.PooledMutableBlockPos.retain();
			for (int neighbourIndex = 0; neighbourIndex < 8; ++neighbourIndex) {
				//local variable for speed
				final Vec3 point = points[neighbourIndex];
				mutablePos.setPos(point.xCoord, point.yCoord, point.zCoord);
				final float neighbourDensity = ModUtil.getBlockDensity(mutablePos, cache);
				neighbourDensities[neighbourIndex] = neighbourDensity;
				final boolean neighborIsInsideIsosurface = neighbourDensity > isosurfaceLevel;
				neighbourMask |= neighborIsInsideIsosurface ? 1 << neighbourIndex : 0;
				if (ModConfig.offsetVertices) {
					ModUtil.givePointRoughness(point);
				}
			}
			mutablePos.release();
		}

		// Check for early termination if cell does not intersect boundary
		// 0x00 = completely inside, 0xFF = completely outside
		if (neighbourMask == 0b00000000) {
			event.setCanceled(ModUtil.shouldSmooth(state));
		} else if (neighbourMask == 0b11111111) {
			if (ModConfig.hideOutsideBlocks) {
				event.setCanceled(ModUtil.shouldSmooth(state));
			}
		}

		// get texture
		for (final BlockPos.MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
			if (ModUtil.shouldSmooth(textureState)) {
				break;
			} else {
				textureState = cache.getBlockState(mutablePos);
				texturePos = mutablePos;
			}
		}

		final BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
		if (quad == null) {
			return;
		}
		final TextureAtlasSprite sprite = quad.getSprite();
		final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
		final int red = (color >> 16) & 0xFF;
		final int green = (color >> 8) & 0xFF;
		final int blue = color & 0xFF;
		final int alpha = 0xFF;

		final double minU = ClientUtil.getMinU(sprite);
		final double minV = ClientUtil.getMinV(sprite);
		final double maxU = ClientUtil.getMaxU(sprite);
		final double maxV = ClientUtil.getMaxV(sprite);

		// real pos not texturePos
		final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);
		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		final BufferBuilder bufferBuilder = event.getBufferBuilder();

		// Sum up edge intersections
		final int edgeMask = EDGE_TABLE[neighbourMask];
		int edgeCrossingCount = 0;
		final float[] vertex = new float[]{0.0F, 0.0F, 0.0F};

		// For every edge of the cube...
		// calculate the interpolated vertex?
		for (int cubeEdgeIndex = 0; cubeEdgeIndex < 12; ++cubeEdgeIndex) {

			// Use edge mask to check if it is crossed
			if ((edgeMask & (1 << cubeEdgeIndex)) == 0) {
				continue;
			}

			// If it did, increment number of edge crossings
			++edgeCrossingCount;

			// Now find the point of intersection
			// The cube edges store the offsets (points) that can contain a smoothable block (I think)
			final int cubeEdge0 = CUBE_EDGES[cubeEdgeIndex << 1]; // Unpack vertices
			final int cubeEdge1 = CUBE_EDGES[(cubeEdgeIndex << 1) + 1];
			final float neighbourDensity0 = neighbourDensities[cubeEdge0]; // Unpack grid values
			final float neighbourDensity1 = neighbourDensities[cubeEdge1];

			float pointOfIntersection_interpolationValue = neighbourDensity0 - neighbourDensity1; // Compute point of intersection
			if (Math.abs(pointOfIntersection_interpolationValue) > 0.0F) {
				pointOfIntersection_interpolationValue = neighbourDensity0 / pointOfIntersection_interpolationValue;
				int axis = 0;

				// Interpolate vertices and add up intersections (this can be done without multiplying)
				// because we are dealing with a float[3] instead of a Vec3 (x, y, z),
				// we use the variable axis to get out index into the float[3]
				for (int bitIndex = 1; axis < 3; bitIndex <<= 1) {
					final int a = cubeEdge0 & bitIndex;
					final int b = cubeEdge1 & bitIndex;
					// perform linear interpolation efficiently by avoiding multiplication
					// this can be done because we know that a and b are either 1 or 0, nothing else
					// this is the same as
					// vertex[axis] += a * (1.0 - pointOfIntersection_interpolationValue) + b * pointOfIntersection_interpolationValue
					if (a != b) {
						vertex[axis] += a != 0 ? 1.0F - pointOfIntersection_interpolationValue : pointOfIntersection_interpolationValue;
					} else {
						vertex[axis] += a != 0 ? 1.0F : 0.0F;
					}
					++axis;
				}
			}
		}

		

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

}
