package me.imbanana.mcdoom.doom;

import java.util.Arrays;
import java.util.Optional;

public enum DoomKey {
    RIGHT_ARROW(0xae, 262),
    LEFT_ARROW(0xac, 263),
    UP_ARROW(0xad, 265, 87),
    DOWN_ARROW(0xaf, 264, 83),
    STRAFE_LEFT(0xa0, 44, 65),
    STRAFE_RIGHT(0xa1, 46, 68),
    USE(0xa2, 32),
    FIRE(0xa3, 345, 265),
    ESCAPE(27, 256),
    ENTER(13, 257),
    TAB(9, 258),
    F1(0x80 + 0x3b, 290),
    F2(0x80 + 0x3c, 291),
    F3(0x80 + 0x3d, 292),
    F4(0x80 + 0x3e, 293),
    F5(0x80 + 0x3f, 294),
    F6(0x80 + 0x40, 295),
    F7(0x80 + 0x41, 296),
    F8(0x80 + 0x42, 297),
    F9(0x80 + 0x43, 298),
    F10(0x80 + 0x44, 299),
    F11(0x80 + 0x57, 300),
    F12(0x80 + 0x58, 301),
    BACKSPACE(0x7f, 92),
    PAUSE(0xff, 284),
    EQUALS(0x3d, 61),
    MINUS(0x2d, 45),
    RIGHT_SHIFT(0x80 + 0x36, 344),
    RIGHT_CTRL(0x80 + 0x1d, 345),
    RIGHT_ALT(0x80 + 0x38, 346),
    LEFT_ALT(RIGHT_ALT.getDoomCode(), 342);

    private final int doomCode;
    private final int mcCode;
    private final int wasdLayoutCode;

    DoomKey(int doomCode, int mcCode, int wasdLayoutCode) {
        this.doomCode = doomCode;
        this.mcCode = mcCode;
        this.wasdLayoutCode = wasdLayoutCode;
    }

    DoomKey(int doomCode, int mcCode) {
        this(doomCode, mcCode, mcCode);
    }

    public int getDoomCode() {
        return doomCode;
    }

    public static Optional<DoomKey> fromMcCode(int code, boolean wasd) {
        return Arrays.stream(DoomKey.values()).filter(doomKey -> wasd ? doomKey.wasdLayoutCode == code : doomKey.mcCode == code).findFirst();
    }
}
