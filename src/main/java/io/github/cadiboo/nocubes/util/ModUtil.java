package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.BlockSnowLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.VersionChecker;

import javax.annotation.Nonnull;
import java.util.Random;

import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.SNOW;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	public static final IIsSmoothable TERRAIN_SMOOTHABLE = ModUtil::shouldSmoothTerrain;
	public static final IIsSmoothable LEAVES_SMOOTHABLE = ModUtil::shouldSmoothLeaves;
	private static final Random RANDOM = new Random();

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	//TODO: inline
	public static boolean shouldSmoothTerrain(final IBlockState state) {
		return IIsSmoothable.TERRAIN_SMOOTHABLE.isSmoothable(state);
	}

	/**
	 * If the state should be smoothed
	 *
	 * @param state the state
	 * @return If the state should be smoothed
	 */
	public static boolean shouldSmoothLeaves(final IBlockState state) {
//		return ModConfig.getLeavesSmoothableBlockStatesCache().contains(state);
		return false;
	}

	/**
	 * @return negative density if the block is smoothable (inside the isosurface), positive if it isn't
	 */
	public static float getIndividualBlockDensity(final boolean shouldSmooth, final IBlockState state) {
		if (shouldSmooth) {
			if (state.getBlock() == SNOW) {
				final int value = state.get(BlockSnowLayer.LAYERS);
				if (value == 1) { // zero-height snow layer
					return 1;
				} else { // snow height between 0-8 to between -0.25F and -1
					return -((value - 1) * 0.125F);
				}
			} else {
				return state.getBlock() == BEDROCK ? -1.0005F : -1;
			}
		} else if (state.isNormalCube() || state.isBlockNormalCube()) {
			return 0F;
		} else {
			return 1;
		}
	}

	/**
	 * Give the vec3 some (pseudo) random offset based on its location.
	 * This code is from {link MathHelper#getCoordinateRandom} and Block#getOffset
	 *
	 * @param vec3 the vec3
	 */
	public static Vec3 offsetVertex(Vec3 vec3) {
		long rand = (long) (vec3.x * 3129871.0D) ^ (long) vec3.z * 116129781L ^ (long) vec3.y;
		rand = rand * rand * 42317861L + rand * 11;
		vec3.x += ((double) ((float) (rand >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
		vec3.y += ((double) ((float) (rand >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
		vec3.z += ((double) ((float) (rand >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
		return vec3;
	}

	@Deprecated
	public static double average(double... values) {
		if (values.length == 0) return 0;

		double total = 0L;

		for (double value : values) {
			total += value;
		}

		return total / values.length;
	}

	/**
	 * Ew
	 *
	 * @param modContainer the {@link ModContainer} for {@link NoCubes}
	 */
	public static void launchUpdateDaemon(@Nonnull final ModContainer modContainer) {

		new Thread(() -> {
			while (true) {

				final VersionChecker.CheckResult checkResult = VersionChecker.getResult(modContainer.getModInfo());
				switch (checkResult.status) {
					default:
					case PENDING:
						try {
							Thread.sleep(500L);
						} catch (InterruptedException var4) {
							Thread.currentThread().interrupt();
						}
						break;
					case OUTDATED:
						try {
							BadAutoUpdater.update(modContainer, checkResult.target.toString(), "Cadiboo");
						} catch (Exception var3) {
							throw new RuntimeException(var3);
						}
					case FAILED:
					case UP_TO_DATE:
					case AHEAD:
					case BETA:
					case BETA_OUTDATED:
						return;
				}
			}

		}, modContainer.getModInfo().getDisplayName() + " Update Daemon").start();

	}

	public static boolean isDeveloperWorkspace() {
		final String target = System.getenv().get("target");
		if (target == null) {
			return false;
		}
		return target.contains("userdev");
	}

}
