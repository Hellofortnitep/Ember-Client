package com.ember.client.emote;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;

/**
 * Each emote poses the player model. t = seconds since the emote started.
 * Arm pitch: negative = raise forward. Right arm roll +: out to the side; left arm roll -: out.
 */
public enum Emote {
    SIX_SEVEN("6 7") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            float s = MathHelper.sin(t * 11f);
            m.rightArm.pitch = -1.35f + 0.55f * s;
            m.leftArm.pitch = -1.35f - 0.55f * s;
            m.rightArm.yaw = -0.25f;
            m.leftArm.yaw = 0.25f;
            m.rightArm.roll = 0f;
            m.leftArm.roll = 0f;
            m.head.pitch = 0.15f + 0.08f * MathHelper.sin(t * 22f);
            m.head.yaw = 0.15f * s;
        }
    },
    WAVE("Wave") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            m.rightArm.pitch = -2.9f;
            m.rightArm.yaw = 0f;
            m.rightArm.roll = 0.35f + 0.45f * MathHelper.sin(t * 9f);
            m.head.yaw = 0.1f * MathHelper.sin(t * 2f);
        }
    },
    DAB("Dab") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            m.rightArm.pitch = -2.3f;
            m.rightArm.yaw = 0f;
            m.rightArm.roll = 1.0f;
            m.leftArm.pitch = -1.7f;
            m.leftArm.yaw = 1.0f;
            m.leftArm.roll = 0f;
            m.head.pitch = 0.6f;
            m.head.yaw = -0.55f;
        }
    },
    FLOSS("Floss") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            float s = MathHelper.sin(t * 12f);
            float c = MathHelper.cos(t * 6f);
            m.rightArm.pitch = 0.55f * c;
            m.leftArm.pitch = 0.55f * c;
            m.rightArm.roll = 0.25f + 0.35f * s;
            m.leftArm.roll = -0.25f + 0.35f * s;
            m.body.yaw = -0.25f * s;
            m.rightLeg.roll = 0.12f * s;
            m.leftLeg.roll = 0.12f * s;
        }
    },
    CLAP("Clap") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            float a = 0.15f + 0.45f * Math.abs(MathHelper.sin(t * 7f));
            m.rightArm.pitch = -1.35f;
            m.leftArm.pitch = -1.35f;
            m.rightArm.yaw = -a;
            m.leftArm.yaw = a;
            m.rightArm.roll = 0f;
            m.leftArm.roll = 0f;
        }
    },
    DANCE("Dance") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            float s = MathHelper.sin(t * 8f);
            m.rightArm.pitch = -0.6f - 1.1f * Math.max(0, s);
            m.leftArm.pitch = -0.6f - 1.1f * Math.max(0, -s);
            m.rightArm.roll = 0.5f + 0.3f * s;
            m.leftArm.roll = -0.5f + 0.3f * s;
            m.rightLeg.pitch = 0.45f * s;
            m.leftLeg.pitch = -0.45f * s;
            m.head.yaw = 0.35f * MathHelper.sin(t * 4f);
            m.head.pitch = 0.1f * MathHelper.cos(t * 16f);
            m.body.yaw = 0.15f * MathHelper.sin(t * 4f);
        }
    },
    TPOSE("T-Pose") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            m.rightArm.pitch = 0f; m.rightArm.yaw = 0f; m.rightArm.roll = 1.5708f;
            m.leftArm.pitch = 0f;  m.leftArm.yaw = 0f;  m.leftArm.roll = -1.5708f;
            m.head.pitch = 0f; m.head.yaw = 0f;
        }
    },
    FACEPALM("Facepalm") {
        @Override void pose(BipedEntityModel<?> m, float t) {
            m.rightArm.pitch = -2.4f;
            m.rightArm.yaw = -0.6f;
            m.rightArm.roll = 0f;
            m.head.pitch = 0.45f + 0.05f * MathHelper.sin(t * 3f);
            m.head.yaw = -0.2f;
        }
    };

    public final String title;

    Emote(String title) { this.title = title; }

    abstract void pose(BipedEntityModel<?> m, float t);

    public void apply(BipedEntityModel<?> m, float t) {
        pose(m, t);
        m.hat.copyTransform(m.head);
    }
}
