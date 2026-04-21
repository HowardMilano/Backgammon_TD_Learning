package training;

import backgammon.Board;
import backgammon.Color;
import backgammon.GenericBoard;
import backgammon.GnuBoard;

//
// Decodes either 20 char GnuBG backgammon board position strings,
// or 14 char backgammon board position strings used by GnuBG and WildBG.
// Ported from the GnuBG sources (many variable names are still the same).
//
// The GnuBoard used is similar to the GnuBG board:
// 0 - 25, 0 - 23 positions, 24 bar, 25 off
// Both players play from 23 - 0.
//
// The Board and GenericBoard classes are my own classes, the Board class
// is my old style board definition, the GenericBoard class translates
// between the two board styles so I can play games with players that
// use the old or the Gnu style boards.
//
public class PositionDecoder {
    private static int Base64(char ch) {
        if (ch >= 'A' && ch <= 'Z')
            return (ch - 'A');

        if (ch >= 'a' && ch <= 'z')
            return (ch - 'a' + 26);

        if (ch >= '0' && ch <= '9')
            return (ch - '0' + 52);

        if (ch == '+')
            return 62;

        if (ch == '/')
            return 63;

        return 255;
    }

    private static GnuBoard boardFromKey(int[] key) throws Exception {
        GnuBoard gnuBoard = new GnuBoard();
        gnuBoard.clearBoard();

        int j = 0;
        int i = 0;
        for (int x = 0; x < 10; x++) {
            int cur = key[x];
            for (int k = 0; k < 8; ++k) {
                if ((cur & 0x1) == 0x1) {
                    if (i >= 2 || j >= 25) {
                        throw new Exception("Error decoding position ID");
                    }
                    gnuBoard.checkers[i][j]++;
                } else {
                    if (++j == 25) {
                        ++i;
                        j = 0;
                    }
                }
                cur >>= 1;
            }
        }

        // Collect off's
        int off0 = 0;
        int off1 = 0;
        for (int x = 0; x < 25; x++) {
            off0 += gnuBoard.checkers[0][x];
            off1 += gnuBoard.checkers[1][x];
        }
        gnuBoard.checkers[0][25] = 15 - off0;
        gnuBoard.checkers[1][25] = 15 - off1;

        gnuBoard.checkInternals();
        gnuBoard.setTurn(Color.BLACK);
        return gnuBoard;
    }

    //
    // Decode 14 char backgammon board position strings used by GnuBG and WildBG
    //
    public static GnuBoard decodePositionFourteenCharID(String positionID) throws Exception {
        char[] pch = new char[positionID.length()];
        for (int i = 0; i < positionID.length(); i++) {
            pch[i] = (char) (PositionDecoder.Base64(positionID.charAt(i)) & 0xFF);
        }
        int[] puch = new int[10];
        int puchPtr = 0, pchPtr = 0;
        for (int i = 0; i < 3; i++) {
            puch[puchPtr++] = ((pch[pchPtr] << 2) | (pch[pchPtr + 1] >> 4)) & 0xFF;
            puch[puchPtr++] = ((pch[pchPtr + 1] << 4) | (pch[pchPtr + 2] >> 2)) & 0xFF;
            puch[puchPtr++] = ((pch[pchPtr + 2] << 6) | pch[pchPtr + 3]) & 0xFF;
            pchPtr += 4;
        }
        puch[puchPtr] = ((pch[pchPtr] << 2) | (pch[pchPtr + 1] >> 4)) & 0xFF;

        return boardFromKey(puch);
    }

    //
    // Decode 20 char backgammon board position strings used by GnuBG
    //
    public static GnuBoard decodePositionTwentyCharID(String positionID) throws Exception {
        int firstConversion[] = new int[10];
        for (int i = 0; i < 10; i++) {
            if (positionID.charAt(2 * i + 0) >= 'A' && positionID.charAt(2 * i + 0) <= 'P' &&
                    positionID.charAt(2 * i + 1) >= 'A' && positionID.charAt(2 * i + 1) <= 'P') {
                firstConversion[i] = ((positionID.charAt(2 * i + 0) - 'A') << 4) +
                        (positionID.charAt(2 * i + 1) - 'A');
                firstConversion[i] &= 0xFF;
            } else {
                throw new Exception("Bad position ID " + positionID);
            }
        }
        return PositionDecoder.boardFromKey(firstConversion);
    }

    /**
     * Basic smoke test for both 20-char and 14-char backgammon position strings.
     * Tests decoding and also encoding from the PositionEncoder.java class
     * 
     */
    public static void main(String[] args) {
        String[] twentyCharIds = {
                "JIGHPAABDAOAHDPAABDA",
                "OAHDOEABCBOAHDPAABDA",
                "OAHDOBABCEOAHDPAABDA",
                "OAHDMJABDAOAHDPAABDA",
                "OAHDPAABAJOAHDPAABDA",
                "OAHDPAABDAOAHDPAABDA",
                "JIGHPAABDAOAHDPAABDA",
                "OAHDOEABCIJIGHPAABDA",
                "OAHDPAABBEJIGHPAABDA",
                "OAHDOCABDAJIGHPAABDA",
                "NAHDOEABDAJIGHPAABDA",
                "NAHDPAABCEJIGHPAABDA",
                "OAGHPAABCIJIGHPAABDA",
                "MIHDPAABCIJIGHPAABDA",
                "JIGHPAABDAOAHDPAABDA",
                "OAHDOEABCIJIGHPAABDA",
                "JIGHPAAJCAOAHDOEABCI",
                "JIGHOCEBCAOAHDOEABCI",
                "JIGHPAEBAEOAHDOEABCI",
                "DIMPOAABDAOAHDOEABCI",
                "DCEPPAABCCOAHDOEABFA",
                "OAHDOEABCIJIGHPAABDA",
                "JIGHOCEBCAOAHDOEABCI"
        };
        String[] fourteenCharIds = {
                "4HPhASjgc/ABMA",
                "4HPwARHgc/ABMA",
                "sGfwATDgc+EBKA",
                "4HPiASjgc+EBKA",
                "4HPDQRCwZ/ABMA",
                "hmfhASiwZ/ABMA",
                "sOfgASTgc8NBEA",
                "aOfgATDgc8NBEA",
                "sGfDQRCw5+ABJA"
        };
        try {
            for (int x = 0; x < twentyCharIds.length; x++) {
                GnuBoard board = decodePositionTwentyCharID(twentyCharIds[x]);
                board.checkInternals();
                GenericBoard genericBoard = board.toGenericBoard();
                Board oldStyleBoard = Board.fromGenericBoard(genericBoard);
                oldStyleBoard.checkInternals();
            }
            for (int x = 0; x < fourteenCharIds.length; x++) {
                GnuBoard board = decodePositionFourteenCharID(fourteenCharIds[x]);
                board.checkInternals();
                GenericBoard genericBoard = board.toGenericBoard();
                Board oldStyleBoard = Board.fromGenericBoard(genericBoard);
                oldStyleBoard.checkInternals();

                // Check that encoding works
                char[] encoding = PositionEncoder.encodePositionFourteenCharID(board);
                String fourteenCharId = new String(fourteenCharIds[x]);
                String encoded = new String(encoding);
                assert fourteenCharId.equals(encoded);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
