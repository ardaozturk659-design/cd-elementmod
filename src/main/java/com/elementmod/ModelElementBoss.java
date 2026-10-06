package com.elementmod;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Kodla animasyonlu EJDERHA modeli: gövde, 3 parçalı boyun, çeneli kafa, boynuzlar,
 * iki parçalı kanatlar, 5 parçalı kuyruk, sırt dikenleri, 4 bacak.
 * Animasyon: nefes, yürüyüş, kanat çırpma, kuyruk sallama, büyü pozu (kafa kalkar, çene açılır, ağızda küre).
 */
public class ModelElementBoss extends ModelBase {
    private final ModelRenderer body, neck1, neck2, neck3, head, jaw, hornR, hornL;
    private final ModelRenderer wingR, wingR2, wingL, wingL2, orb;
    private final ModelRenderer legFR, legFL, legBR, legBL;
    private final ModelRenderer[] tail = new ModelRenderer[5];
    private boolean casting;

    private ModelRenderer part(int u, int v) { return new ModelRenderer(this, u, v); }

    public ModelElementBoss(Element el) {
        textureWidth = 128;
        textureHeight = 128;

        body = part(0, 32);
        body.addBox(-6, -5, -12, 12, 10, 24);
        body.setRotationPoint(0, 10, 0);

        // boyun (3 parça) + kafa
        neck1 = part(0, 70); neck1.addBox(-3, -3, -6, 6, 6, 6); neck1.setRotationPoint(0, -2, -11); body.addChild(neck1);
        neck2 = part(0, 70); neck2.addBox(-3, -3, -6, 6, 6, 6); neck2.setRotationPoint(0, 0, -6); neck1.addChild(neck2);
        neck3 = part(0, 70); neck3.addBox(-3, -3, -6, 6, 6, 6); neck3.setRotationPoint(0, 0, -6); neck2.addChild(neck3);
        head = part(0, 0); head.addBox(-4, -3, -8, 8, 6, 8); head.setRotationPoint(0, 0, -6); neck3.addChild(head);
        jaw = part(32, 0); jaw.addBox(-3, 0, -7, 6, 2, 7); jaw.setRotationPoint(0, 3, -1); head.addChild(jaw);

        float spread = el == Element.WATER ? 0.8F : 0.3F; // su ejderinde yüzgeç gibi açık
        hornR = part(60, 0); hornR.addBox(-0.5F, -5F, -0.5F, 1, 5, 1);
        hornR.setRotationPoint(-3, -3, -1); hornR.rotateAngleX = -0.7F; hornR.rotateAngleZ = -spread;
        hornL = part(60, 0); hornL.mirror = true; hornL.addBox(-0.5F, -5F, -0.5F, 1, 5, 1);
        hornL.setRotationPoint(3, -3, -1); hornL.rotateAngleX = -0.7F; hornL.rotateAngleZ = spread;
        if (el != Element.LIGHT) { head.addChild(hornR); head.addChild(hornL); }

        orb = part(100, 40); orb.addBox(-3, -3, -3, 6, 6, 6); orb.setRotationPoint(0, 0, -12); head.addChild(orb);

        // sırt dikenleri
        int[] sz = {-9, -5, -1, 3, 7, 11};
        for (int z : sz) {
            ModelRenderer s = part(100, 0);
            s.addBox(-1, -3, -1, 2, 3, 2);
            s.setRotationPoint(0, -5, z);
            body.addChild(s);
        }

        // kanatlar (kol + zar, iki parça)
        wingR = part(60, 70); wingR.addBox(-14, -1, -1, 14, 2, 2); wingR.setRotationPoint(-5, -3, -4);
        ModelRenderer wrm = part(0, 110); wrm.addBox(-14, 0, -1, 14, 1, 12); wingR.addChild(wrm);
        wingR2 = part(60, 76); wingR2.addBox(-16, -1, -1, 16, 2, 2); wingR2.setRotationPoint(-14, 0, 0); wingR.addChild(wingR2);
        ModelRenderer wrm2 = part(60, 110); wrm2.addBox(-16, 0, -1, 16, 1, 10); wingR2.addChild(wrm2);
        body.addChild(wingR);

        wingL = part(60, 70); wingL.mirror = true; wingL.addBox(0, -1, -1, 14, 2, 2); wingL.setRotationPoint(5, -3, -4);
        ModelRenderer wlm = part(0, 110); wlm.mirror = true; wlm.addBox(0, 0, -1, 14, 1, 12); wingL.addChild(wlm);
        wingL2 = part(60, 76); wingL2.mirror = true; wingL2.addBox(0, -1, -1, 16, 2, 2); wingL2.setRotationPoint(14, 0, 0); wingL.addChild(wingL2);
        ModelRenderer wlm2 = part(60, 110); wlm2.mirror = true; wlm2.addBox(0, 0, -1, 16, 1, 10); wingL2.addChild(wlm2);
        body.addChild(wingL);

        // kuyruk (5 parça, giderek incelir)
        int[] ts = {6, 5, 4, 3, 2};
        float[] to = {-3F, -2.5F, -2F, -1.5F, -1F};
        for (int i = 0; i < 5; i++) {
            tail[i] = part(30, 70);
            tail[i].addBox(to[i], to[i], 0, ts[i], ts[i], 8);
            tail[i].setRotationPoint(0, 0, i == 0 ? 12 : 8);
            if (i == 0) body.addChild(tail[i]); else tail[i - 1].addChild(tail[i]);
        }

        // bacaklar
        legFR = part(0, 90); legFR.addBox(-2, 0, -2, 4, 9, 4); legFR.setRotationPoint(-5, 15, -7);
        legFL = part(0, 90); legFL.mirror = true; legFL.addBox(-2, 0, -2, 4, 9, 4); legFL.setRotationPoint(5, 15, -7);
        legBR = part(0, 90); legBR.addBox(-2, 0, -2, 4, 9, 4); legBR.setRotationPoint(-5, 15, 7);
        legBL = part(0, 90); legBL.mirror = true; legBL.addBox(-2, 0, -2, 4, 9, 4); legBL.setRotationPoint(5, 15, 7);
    }

    @Override
    public void setRotationAngles(float ls, float lsa, float age, float yaw, float pitch, float scale, Entity en) {
        int cast = en instanceof EntityElementBoss ? ((EntityElementBoss) en).getCast() : 0;
        casting = cast > 0;
        boolean walking = lsa > 0.1F;
        float rad = yaw * 0.017453292F;

        // boyun + kafa
        neck1.rotateAngleX = casting ? -0.9F : -0.5F + MathHelper.sin(age * 0.05F) * 0.05F;
        neck2.rotateAngleX = casting ? -0.5F : -0.3F;
        neck3.rotateAngleX = casting ? -0.3F : -0.1F;
        head.rotateAngleX = casting ? 0.3F : 0.7F;
        neck1.rotateAngleY = rad * 0.3F;
        neck2.rotateAngleY = rad * 0.3F;
        neck3.rotateAngleY = rad * 0.4F;
        jaw.rotateAngleX = casting ? 0.7F + MathHelper.sin(age * 0.9F) * 0.1F : 0.05F + MathHelper.sin(age * 0.1F) * 0.03F;

        // nefes
        body.rotationPointY = 10F + MathHelper.sin(age * 0.07F) * 0.4F;

        // bacaklar
        float sw = MathHelper.cos(ls * 0.6662F) * 1.0F * lsa;
        legFR.rotateAngleX = sw; legBL.rotateAngleX = sw;
        legFL.rotateAngleX = -sw; legBR.rotateAngleX = -sw;

        // kanatlar
        float base = casting ? 0.5F : 0.25F;
        float speed = casting ? 0.6F : (walking ? 0.2F : 0.12F);
        float amp = casting ? 0.7F : (walking ? 0.35F : 0.22F);
        float f = MathHelper.sin(age * speed) * amp;
        wingR.rotateAngleZ = base + f;
        wingL.rotateAngleZ = -(base + f);
        wingR2.rotateAngleZ = f * 0.6F;
        wingL2.rotateAngleZ = -f * 0.6F;
        wingR.rotateAngleY = 0.35F;
        wingL.rotateAngleY = -0.35F;

        // kuyruk
        for (int i = 0; i < tail.length; i++) {
            tail[i].rotateAngleY = MathHelper.sin(age * 0.1F - i * 0.7F) * 0.22F;
        }

        // büyü küresi
        orb.showModel = casting;
        orb.rotateAngleY = age * 0.3F;
        orb.rotateAngleX = age * 0.2F;
    }

    @Override
    public void render(Entity en, float ls, float lsa, float age, float yaw, float pitch, float scale) {
        setRotationAngles(ls, lsa, age, yaw, pitch, scale, en);
        body.render(scale);
        legFR.render(scale);
        legFL.render(scale);
        legBR.render(scale);
        legBL.render(scale);
    }
}
