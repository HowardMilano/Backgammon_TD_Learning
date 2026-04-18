public class GnuBgDecoder {
    //
    // Decode 20 character position code positions used in GnuBG
    // training and benchmark files.
    //
    public static GnuBoard decodePositionID(String positionID) {

        //
        // Assumes a GnuBG style board, 0 - 25, 24 is bar, 25 is off
        // Both players go from 23 - 0
        //
        GnuBoard gnuBoard = new GnuBoard();
        gnuBoard.clearBoard();

        // Convert to 10 character position string
        int firstConversion[] = new int[10];
        for (int i = 0; i < 10; i++) {
            if (positionID.charAt(2 * i + 0) >= 'A' && positionID.charAt(2 * i + 0) <= 'P' &&
                    positionID.charAt(2 * i + 1) >= 'A' && positionID.charAt(2 * i + 1) <= 'P') {
                firstConversion[i] = ((positionID.charAt(2 * i + 0) - 'A') << 4) +
                        (positionID.charAt(2 * i + 1) - 'A');
                firstConversion[i] &= 0xFF;
            } else {
                System.out.println("Bad position ID " + positionID);
                System.exit(1);
            }
        }
        // Convert to board position
        int j = 0;
        int i = 0;
        for (int x = 0; x < 10; x++) {
            int cur = firstConversion[x];
            for (int k = 0; k < 8; ++k) {
                if ((cur & 0x1) == 0x1) {
                    if (i >= 2 || j >= 25) {
                        System.out.println("Error decoding position ID");
                        System.exit(1);
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

        // Compute off's
        int off0 = 0;
        int off1 = 0;
        for (int x = 0; x < 25; x++) {
            off0 += gnuBoard.checkers[0][x];
            off1 += gnuBoard.checkers[1][x];
        }
        gnuBoard.checkers[0][25] = 15 - off0;
        gnuBoard.checkers[1][25] = 15 - off1;

        gnuBoard.setTurn(Color.BLACK);
        gnuBoard.checkInternals();
        return gnuBoard;
    }

    public static void main(String[] args) {
        // Example: Starting Position ID
        String[] ids = {
                "r JIGHPAABDAOAHDPAABDA",
                "r OAHDOEABCBOAHDPAABDA",
                "r OAHDOBABCEOAHDPAABDA",
                "r OAHDMJABDAOAHDPAABDA",
                "r OAHDPAABAJOAHDPAABDA",
                "m OAHDPAABDAOAHDPAABDA",
                "o JIGHPAABDAOAHDPAABDA",
                "r OAHDOEABCIJIGHPAABDA",
                "r OAHDPAABBEJIGHPAABDA",
                "r OAHDOCABDAJIGHPAABDA",
                "r NAHDOEABDAJIGHPAABDA",
                "r NAHDPAABCEJIGHPAABDA",
                "r OAGHPAABCIJIGHPAABDA",
                "r MIHDPAABCIJIGHPAABDA",
                "m JIGHPAABDAOAHDPAABDA",
                "o OAHDOEABCIJIGHPAABDA",
                "r JIGHPAAJCAOAHDOEABCI",
                "r JIGHOCEBCAOAHDOEABCI",
                "r JIGHPAEBAEOAHDOEABCI",
                "r DIMPOAABDAOAHDOEABCI",
                "r DCEPPAABCCOAHDOEABFA",
                "m OAHDOEABCIJIGHPAABDA",
                "o JIGHOCEBCAOAHDOEABCI"
        };

        for (int x = 0; x < ids.length; x++) {
            GnuBoard gnuBoard = decodePositionID(ids[x].substring(2));
            // I have two board style, gnu style and old style, make sure it's a valid board in the old style
            GenericBoard genericBoard = gnuBoard.toGenericBoard();
            Board oldStyleBoard = Board.fromGenericBoard(genericBoard);
            oldStyleBoard.checkInternals();

            if (ids[x].startsWith("m") && !ids[x - 1].startsWith("m"))
                System.out.println();
            System.out.println(
                    ids[x].substring(0, 2) + "P0 Points 0-23 + Bar + Off: "
                            + Arrays.toString(Arrays.copyOfRange(gnuBoard.checkers[0], 0, 26)));
            System.out.println(
                    ids[x].substring(0, 2) + "P1 Points 0-23 + Bar + Off: "
                            + Arrays.toString(Arrays.copyOfRange(gnuBoard.checkers[1], 0, 26)));
        }
    }
}
