package io.github.cadiboo.nocubes.debug;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.List;

/**
 * @author Cadiboo
 */
public final class SmoothLightingFluid {

	private static final Field fluidRenderer;
	static {
		fluidRenderer = ObfuscationReflectionHelper.findField(BlockRendererDispatcher.class, "field_175025_e");
		fluidRenderer.setAccessible(true);
	}

	public static void changeFluidRenderer() {
//		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//		try {
//			fluidRenderer.set(blockRendererDispatcher, new SmoothLightingFluidRenderer(Minecraft.getMinecraft().getBlockColors()));
//		} catch (IllegalAccessException e) {
//			// Some other mod messed up and reset the access flag of the field.
//			CrashReport crashReport = new CrashReport("An impossible error has occurred!", e);
//			crashReport.makeCategory("Reflectively Accessing BlockRendererDispatcher#fluidRenderer");
//			throw new ReportedException(crashReport);
//		}
	}

	private static class SmoothLightingFluidRenderer extends BlockFluidRenderer {

		private static final Class<?> BlockModelRenderer_AmbientOcclusionFace = ReflectionHelper.getClass(Loader.instance().getModClassLoader(), "AmbientOcclusionFace");
		private static final Constructor<?> BlockModelRenderer_AmbientOcclusionFace_constructor = ObfuscationReflectionHelper.findConstructor(BlockModelRenderer_AmbientOcclusionFace);
		private final BlockColors blockColors;

		private final TextureAtlasSprite[] atlasSpritesLava = new TextureAtlasSprite[2];
		private final TextureAtlasSprite[] atlasSpritesWater = new TextureAtlasSprite[2];
		private TextureAtlasSprite atlasSpriteWaterOverlay;

		SmoothLightingFluidRenderer(final BlockColors blockColorsIn) {
			super(blockColorsIn);
			this.blockColors = blockColorsIn;

			TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
			this.atlasSpritesLava[0] = texturemap.getAtlasSprite("minecraft:blocks/lava_still");
			this.atlasSpritesLava[1] = texturemap.getAtlasSprite("minecraft:blocks/lava_flow");
			this.atlasSpritesWater[0] = texturemap.getAtlasSprite("minecraft:blocks/water_still");
			this.atlasSpritesWater[1] = texturemap.getAtlasSprite("minecraft:blocks/water_flow");
			this.atlasSpriteWaterOverlay = texturemap.getAtlasSprite("minecraft:blocks/water_overlay");

		}

		@Override
		public boolean renderFluid(final IBlockAccess blockAccess, final IBlockState blockStateIn, final BlockPos blockPosIn, final BufferBuilder bufferBuilderIn) {
			BlockLiquid blockliquid = (BlockLiquid) blockStateIn.getBlock();
			boolean isLava = blockStateIn.getMaterial() == Material.LAVA;
			TextureAtlasSprite[] sprites = isLava ? this.atlasSpritesLava : this.atlasSpritesWater;

			int colorMultiplier = blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn, 0);
			float red = (float) (colorMultiplier >> 16 & 255) / 255.0F;
			float green = (float) (colorMultiplier >> 8 & 255) / 255.0F;
			float blue = (float) (colorMultiplier & 255) / 255.0F;

			boolean shouldRenderUp = blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.UP);
			boolean shouldRenderDown = blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.DOWN);
			boolean[] renderSides = new boolean[]{blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.NORTH), blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.SOUTH), blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.WEST), blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.EAST)};

			if (!shouldRenderUp && !shouldRenderDown && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3]) {
				return false;
			} else {
				boolean wasAnythingRendered = false;
//				float f3 = 0.5F;
//				float f4 = 1.0F;
//				float f5 = 0.8F;
//				float f6 = 0.6F;
				Material material = blockStateIn.getMaterial();
				float fluidHeight = getFluidHeight(blockAccess, blockPosIn, material);
				float fluidHeightSouth = getFluidHeight(blockAccess, blockPosIn.south(), material);
				float fluidHeightEastSouth = getFluidHeight(blockAccess, blockPosIn.east().south(), material);
				float fluidHeightEast = getFluidHeight(blockAccess, blockPosIn.east(), material);
				double x = (double) blockPosIn.getX();
				double y = (double) blockPosIn.getY();
				double z = (double) blockPosIn.getZ();
//				float f11 = 0.001F;

				if (shouldRenderUp) {
					wasAnythingRendered = true;
					float slopeAngle = BlockLiquid.getSlopeAngle(blockAccess, blockPosIn, material, blockStateIn);
					TextureAtlasSprite textureAtlasSprite = slopeAngle > -999.0F ? sprites[1] : sprites[0];
					fluidHeight -= 0.001F;
					fluidHeightSouth -= 0.001F;
					fluidHeightEastSouth -= 0.001F;
					fluidHeightEast -= 0.001F;
					float minU;
					float u_f14;
					float maxU;
					float u_f16;
					float minV;
					float maxV;
					float v_f19;
					float v_f20;

					if (slopeAngle < -999.0F) {
						minU = textureAtlasSprite.getInterpolatedU(0.0D);
						minV = textureAtlasSprite.getInterpolatedV(0.0D);
						u_f14 = minU;
						maxV = textureAtlasSprite.getInterpolatedV(16.0D);
						maxU = textureAtlasSprite.getInterpolatedU(16.0D);
						v_f19 = maxV;
						u_f16 = maxU;
						v_f20 = minV;
					} else {
						float sinSlopeAngle = MathHelper.sin(slopeAngle) * 0.25F;
						float cosSlopeAngle = MathHelper.cos(slopeAngle) * 0.25F;
//						float f23 = 8.0F;
						minU = textureAtlasSprite.getInterpolatedU((double) (8.0F + (-cosSlopeAngle - sinSlopeAngle) * 16.0F));
						minV = textureAtlasSprite.getInterpolatedV((double) (8.0F + (-cosSlopeAngle + sinSlopeAngle) * 16.0F));
						u_f14 = textureAtlasSprite.getInterpolatedU((double) (8.0F + (-cosSlopeAngle + sinSlopeAngle) * 16.0F));
						maxV = textureAtlasSprite.getInterpolatedV((double) (8.0F + (cosSlopeAngle + sinSlopeAngle) * 16.0F));
						maxU = textureAtlasSprite.getInterpolatedU((double) (8.0F + (cosSlopeAngle + sinSlopeAngle) * 16.0F));
						v_f19 = textureAtlasSprite.getInterpolatedV((double) (8.0F + (cosSlopeAngle - sinSlopeAngle) * 16.0F));
						u_f16 = textureAtlasSprite.getInterpolatedU((double) (8.0F + (cosSlopeAngle - sinSlopeAngle) * 16.0F));
						v_f20 = textureAtlasSprite.getInterpolatedV((double) (8.0F + (-cosSlopeAngle - sinSlopeAngle) * 16.0F));
					}

					int packedLightmapCoords = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn);
					int skyLight = packedLightmapCoords >> 16 & 65535;
					int blockLight = packedLightmapCoords & 65535;
					float redFloat = 1.0F * red;
					float greenFloat = 1.0F * green;
					float blueFloat = 1.0F * blue;
					bufferBuilderIn.pos(x + 0.0D, y + (double) fluidHeight, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
					bufferBuilderIn.pos(x + 0.0D, y + (double) fluidHeightSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f14, (double) maxV).lightmap(skyLight, blockLight).endVertex();
					bufferBuilderIn.pos(x + 1.0D, y + (double) fluidHeightEastSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) maxU, (double) v_f19).lightmap(skyLight, blockLight).endVertex();
					bufferBuilderIn.pos(x + 1.0D, y + (double) fluidHeightEast, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f16, (double) v_f20).lightmap(skyLight, blockLight).endVertex();

					if (blockliquid.shouldRenderSides(blockAccess, blockPosIn.up())) {
						bufferBuilderIn.pos(x + 0.0D, y + (double) fluidHeight, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
						bufferBuilderIn.pos(x + 1.0D, y + (double) fluidHeightEast, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f16, (double) v_f20).lightmap(skyLight, blockLight).endVertex();
						bufferBuilderIn.pos(x + 1.0D, y + (double) fluidHeightEastSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) maxU, (double) v_f19).lightmap(skyLight, blockLight).endVertex();
						bufferBuilderIn.pos(x + 0.0D, y + (double) fluidHeightSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f14, (double) maxV).lightmap(skyLight, blockLight).endVertex();
					}
				}

				if (shouldRenderDown) {
					wasAnythingRendered = renderFluidDown(sprites, blockStateIn, blockAccess, blockPosIn, bufferBuilderIn, x, y, z);
				}

				for (int posAddIndex = 0; posAddIndex < 4; ++posAddIndex) {
					int addX = 0;
					int addY = 0;

					if (posAddIndex == 0) {
						--addY;
					}

					if (posAddIndex == 1) {
						++addY;
					}

					if (posAddIndex == 2) {
						--addX;
					}

					if (posAddIndex == 3) {
						++addX;
					}

					BlockPos blockpos = blockPosIn.add(addX, 0, addY);
					TextureAtlasSprite textureatlassprite1 = sprites[1];

					if (!isLava) {
						IBlockState state = blockAccess.getBlockState(blockpos);

						if (state.getBlockFaceShape(blockAccess, blockpos, EnumFacing.VALUES[posAddIndex + 2].getOpposite()) == net.minecraft.block.state.BlockFaceShape.SOLID) {
							textureatlassprite1 = this.atlasSpriteWaterOverlay;
						}
					}

					if (renderSides[posAddIndex]) {
						float yAdd_f39;
						float yAdd_f40;
						double x_d3;
						double z_d4;
						double x_d5;
						double z_d6;

						if (posAddIndex == 0) {
							yAdd_f39 = fluidHeight;
							yAdd_f40 = fluidHeightEast;
							x_d3 = x;
							x_d5 = x + 1.0D;
							z_d4 = z + 0.0010000000474974513D;
							z_d6 = z + 0.0010000000474974513D;
						} else if (posAddIndex == 1) {
							yAdd_f39 = fluidHeightEastSouth;
							yAdd_f40 = fluidHeightSouth;
							x_d3 = x + 1.0D;
							x_d5 = x;
							z_d4 = z + 1.0D - 0.0010000000474974513D;
							z_d6 = z + 1.0D - 0.0010000000474974513D;
						} else if (posAddIndex == 2) {
							yAdd_f39 = fluidHeightSouth;
							yAdd_f40 = fluidHeight;
							x_d3 = x + 0.0010000000474974513D;
							x_d5 = x + 0.0010000000474974513D;
							z_d4 = z + 1.0D;
							z_d6 = z;
						} else {
							yAdd_f39 = fluidHeightEast;
							yAdd_f40 = fluidHeightEastSouth;
							x_d3 = x + 1.0D - 0.0010000000474974513D;
							x_d5 = x + 1.0D - 0.0010000000474974513D;
							z_d4 = z;
							z_d6 = z + 1.0D;
						}

						wasAnythingRendered = true;
						float u_f41 = textureatlassprite1.getInterpolatedU(0.0D);
						float u_f27 = textureatlassprite1.getInterpolatedU(8.0D);
						float f28 = textureatlassprite1.getInterpolatedV((double) ((1.0F - yAdd_f39) * 16.0F * 0.5F));
						float v_f29 = textureatlassprite1.getInterpolatedV((double) ((1.0F - yAdd_f40) * 16.0F * 0.5F));
						float v_f30 = textureatlassprite1.getInterpolatedV(8.0D);
						int packedLightmapCoords = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos);
						int skyLight = packedLightmapCoords >> 16 & 65535;
						int blockLight = packedLightmapCoords & 65535;
						float colorFloatMultiplier = posAddIndex < 2 ? 0.8F : 0.6F;
						float redFloat = 1.0F * colorFloatMultiplier * red;
						float greenFloat = 1.0F * colorFloatMultiplier * green;
						float blueFloat = 1.0F * colorFloatMultiplier * blue;
						bufferBuilderIn.pos(x_d3, y + (double) yAdd_f39, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) f28).lightmap(skyLight, blockLight).endVertex();
						bufferBuilderIn.pos(x_d5, y + (double) yAdd_f40, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f29).lightmap(skyLight, blockLight).endVertex();
						bufferBuilderIn.pos(x_d5, y + 0.0D, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f30).lightmap(skyLight, blockLight).endVertex();
						bufferBuilderIn.pos(x_d3, y + 0.0D, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) v_f30).lightmap(skyLight, blockLight).endVertex();

						if (textureatlassprite1 != this.atlasSpriteWaterOverlay) {
							bufferBuilderIn.pos(x_d3, y + 0.0D, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) v_f30).lightmap(skyLight, blockLight).endVertex();
							bufferBuilderIn.pos(x_d5, y + 0.0D, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f30).lightmap(skyLight, blockLight).endVertex();
							bufferBuilderIn.pos(x_d5, y + (double) yAdd_f40, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f29).lightmap(skyLight, blockLight).endVertex();
							bufferBuilderIn.pos(x_d3, y + (double) yAdd_f39, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) f28).lightmap(skyLight, blockLight).endVertex();
						}
					}
				}

				return wasAnythingRendered;
			}
		}

		private boolean renderFluidDown(TextureAtlasSprite[] sprites, IBlockState blockStateIn, IBlockAccess blockAccess, BlockPos blockPosIn, BufferBuilder bufferBuilderIn, double x, double y, double z) {
			float minU = sprites[0].getMinU();
			float maxU = sprites[0].getMaxU();
			float minV = sprites[0].getMinV();
			float maxV = sprites[0].getMaxV();
			int packedLightmapCoords = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down());
			int skyLight = packedLightmapCoords >> 16 & 65535;
			int blockLight = packedLightmapCoords & 65535;
			bufferBuilderIn.pos(x, y, z + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(x, y, z).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(x + 1.0D, y, z).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) minV).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(x + 1.0D, y, z + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
			return true;
		}

		static float getFluidHeight(IBlockAccess blockAccess, BlockPos blockPosIn, Material blockMaterial) {
			int divisor = 0;
			float liquidHeightPercentage = 0.0F;

			for (int posAdd = 0; posAdd < 4; ++posAdd) {
				BlockPos blockpos = blockPosIn.add(-(posAdd & 1), 0, -(posAdd >> 1 & 1));

				if (blockAccess.getBlockState(blockpos.up()).getMaterial() == blockMaterial) {
					return 1.0F;
				}

				IBlockState iblockstate = blockAccess.getBlockState(blockpos);
				Material material = iblockstate.getMaterial();

				if (material != blockMaterial) {
					if (!material.isSolid()) {
						++liquidHeightPercentage;
						++divisor;
					}
				} else {
					int liquidLevel = iblockstate.getValue(BlockLiquid.LEVEL);

					if (liquidLevel >= 8 || liquidLevel == 0) {
						liquidHeightPercentage += BlockLiquid.getLiquidHeightPercent(liquidLevel) * 10.0F;
						divisor += 10;
					}

					liquidHeightPercentage += BlockLiquid.getLiquidHeightPercent(liquidLevel);
					++divisor;
				}
			}

			return 1.0F - liquidHeightPercentage / (float) divisor;
		}

		public boolean renderModelSmooth(IBlockAccess worldIn, IBakedModel modelIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, boolean checkSides, long rand) throws IllegalAccessException, InvocationTargetException, InstantiationException {
//			BlockModelRenderer.AmbientOcclusionFace
			boolean flag = false;
			float[] afloat = new float[EnumFacing.values().length * 2];
			BitSet bitset = new BitSet(3);
			Object blockmodelrenderer$ambientocclusionface = BlockModelRenderer_AmbientOcclusionFace_constructor.newInstance();

			for (EnumFacing enumfacing : EnumFacing.values())
			{
				List<BakedQuad> list = modelIn.getQuads(stateIn, enumfacing, rand);

				if (!list.isEmpty() && (!checkSides || stateIn.shouldSideBeRendered(worldIn, posIn, enumfacing)))
				{
					this.renderQuadsSmooth(worldIn, stateIn, posIn, buffer, list, afloat, bitset, blockmodelrenderer$ambientocclusionface);
					flag = true;
				}
			}

			List<BakedQuad> list1 = modelIn.getQuads(stateIn, null, rand);

			if (!list1.isEmpty())
			{
				this.renderQuadsSmooth(worldIn, stateIn, posIn, buffer, list1, afloat, bitset, blockmodelrenderer$ambientocclusionface);
				flag = true;
			}

			return flag;
		}

		private void renderQuadsSmooth(IBlockAccess blockAccessIn, IBlockState stateIn, BlockPos posIn, BufferBuilder buffer, List<BakedQuad> list, float[] quadBounds, BitSet bitSet, Object aoFace)
		{
//			Vec3d vec3d = stateIn.getOffset(blockAccessIn, posIn);
//			double d0 = (double)posIn.getX() + vec3d.x;
//			double d1 = (double)posIn.getY() + vec3d.y;
//			double d2 = (double)posIn.getZ() + vec3d.z;
//			int i = 0;
//
//			for (int j = list.size(); i < j; ++i)
//			{
//				BakedQuad bakedquad = list.get(i);
//				this.fillQuadBounds(stateIn, bakedquad.getVertexData(), bakedquad.getFace(), quadBounds, bitSet);
//				aoFace.updateVertexBrightness(blockAccessIn, stateIn, posIn, bakedquad.getFace(), quadBounds, bitSet);
//				buffer.addVertexData(bakedquad.getVertexData());
//				buffer.putBrightness4(aoFace.vertexBrightness[0], aoFace.vertexBrightness[1], aoFace.vertexBrightness[2], aoFace.vertexBrightness[3]);
//				if(bakedquad.shouldApplyDiffuseLighting())
//				{
//					float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(bakedquad.getFace());
//					aoFace.vertexColorMultiplier[0] *= diffuse;
//					aoFace.vertexColorMultiplier[1] *= diffuse;
//					aoFace.vertexColorMultiplier[2] *= diffuse;
//					aoFace.vertexColorMultiplier[3] *= diffuse;
//				}
//				if (bakedquad.hasTintIndex())
//				{
//					int k = this.blockColors.colorMultiplier(stateIn, blockAccessIn, posIn, bakedquad.getTintIndex());
//
//					if (EntityRenderer.anaglyphEnable)
//					{
//						k = TextureUtil.anaglyphColor(k);
//					}
//
//					float f = (float)(k >> 16 & 255) / 255.0F;
//					float f1 = (float)(k >> 8 & 255) / 255.0F;
//					float f2 = (float)(k & 255) / 255.0F;
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[0] * f, aoFace.vertexColorMultiplier[0] * f1, aoFace.vertexColorMultiplier[0] * f2, 4);
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[1] * f, aoFace.vertexColorMultiplier[1] * f1, aoFace.vertexColorMultiplier[1] * f2, 3);
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[2] * f, aoFace.vertexColorMultiplier[2] * f1, aoFace.vertexColorMultiplier[2] * f2, 2);
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[3] * f, aoFace.vertexColorMultiplier[3] * f1, aoFace.vertexColorMultiplier[3] * f2, 1);
//				}
//				else
//				{
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[0], aoFace.vertexColorMultiplier[0], 4);
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[1], aoFace.vertexColorMultiplier[1], 3);
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[2], aoFace.vertexColorMultiplier[2], 2);
//					buffer.putColorMultiplier(aoFace.vertexColorMultiplier[3], aoFace.vertexColorMultiplier[3], aoFace.vertexColorMultiplier[3], 1);
//				}
//
//				buffer.putPosition(d0, d1, d2);
//			}
		}

	}

}
