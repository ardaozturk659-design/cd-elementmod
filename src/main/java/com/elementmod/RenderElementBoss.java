package com.elementmod;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderElementBoss extends RenderLiving<EntityElementBoss> {
    private final Element el;
    private final ResourceLocation tex;

    public RenderElementBoss(RenderManager m, Element el) {
        super(m, new ModelElementBoss(el), 0.5f * EntityElementBoss.scaleOf(el));
        this.el = el;
        this.tex = new ResourceLocation(ElementMod.MODID, "textures/entity/" + el.id + "_boss.png");
    }

    @Override
    protected void preRenderCallback(EntityElementBoss e, float partialTicks) {
        float s = EntityElementBoss.scaleOf(el);
        GlStateManager.scale(s, s, s);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityElementBoss e) { return tex; }
}
