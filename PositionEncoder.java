package training;

import backgammon.Color;
import backgammon.GnuBoard;

//
// Encodes a 14 char backgammon board position strings used by GnuBG and WildBG.
// Ported from the GnuBG sources (many of the variable names are still the same).
//
// The GnuBoard used is similar to the GnuBG board:
// 0 - 25, 0 - 23 positions, 24 bar, 25 off
// Both players play from 23 - 0.
//
// This class has a basic smoke test in PositionDecoder.java
//
public class PositionEncoder {
    private static void addBits(char auchKey[], int bitPos, int nBits) {
        int k = (bitPos / 8) & 0xF;
        int r = (bitPos & 0x7);
        int b = (((0x1 << nBits) - 1) << r);
        auchKey[k] |= (char) (b & 0xFF);
        if (k < 8) {
            auchKey[k + 1] |= (char) ((b >> 8) & 0xFF);
            auchKey[k + 2] |= (char) ((b >> 16) & 0xFF);
        } else if (k == 8) {
            auchKey[k + 1] |= (char) ((b >> 8) & 0xFF);
        }
    }

    //
    // Encode 14 char backgammon board position strings used by GnuBG and WildBG
    //
    public static char[] encodePositionFourteenCharID(GnuBoard board) {
        // Flip the board such that the POV matches the GnuBG POV
        GnuBoard finalBoard = board;
        if (board.getTurn() == Color.WHITE) {
            finalBoard = new GnuBoard(board);
            finalBoard.flipPerspective();
        }

        // First step
        char[] auchKey = new char[10];
        int iBit = 0;
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 25; j++) {
                if (finalBoard.checkers[i][j] > 0) {
                    addBits(auchKey, iBit, finalBoard.checkers[i][j]);
                    iBit += finalBoard.checkers[i][j];
                }
                addBits(auchKey, iBit, 0);
                iBit += 1;
            }
        }
        // Second step
        String aszBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        char[] szID = new char[14];
        int pointer = 0;
        int auchKeyPtr = 0;
        int index;
        for (int i = 0; i < 3; i++) {
            index = (auchKey[auchKeyPtr] >> 2) & 0xFF;
            szID[pointer++] = aszBase64.charAt(index);
            index = (((auchKey[auchKeyPtr] & 0x03) << 4) | (auchKey[auchKeyPtr+1] >> 4)) & 0xFF;
            szID[pointer++] = aszBase64.charAt(index);
            index = (((auchKey[auchKeyPtr+1] & 0x0F) << 2) | (auchKey[auchKeyPtr+2] >> 6)) & 0xFF;
            szID[pointer++] = aszBase64.charAt(index);
            index = ((auchKey[auchKeyPtr+2] & 0x3F)) & 0xFF;
            szID[pointer++] = aszBase64.charAt(index);
            auchKeyPtr += 3;
        }
        index = (auchKey[auchKeyPtr] >> 2) & 0xFF;
        szID[pointer++] = aszBase64.charAt(index);
        index = ((auchKey[auchKeyPtr] & 0x03) << 4) & 0xFF;
        szID[pointer++] = aszBase64.charAt(index);
        return szID;
    }
}
