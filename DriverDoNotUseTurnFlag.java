//
// Update: This driver doesn't perform as well as the other 2 drivers.
//
public class DriverDoNotUseTurnFlag {
    private static double epsilon = 0.09; // How often should we pick a random move?

    //
    // Assumes a GnuBG style board, 0 - 23, 24 for bar, 25 for off
    // Both players play from 23 - 0
    //
    
    public static double[] getBoardNotation(GnuBoard gameBoard, Color player) {
        if (player == Color.WHITE) {
            // Do not add the turn flag setting to the board notation
            return gameBoard.getBoardNotation(false);
        }
        else {
            GnuBoard copy = new GnuBoard(gameBoard);
            // Flip the board mirror-like, so swap left and right sides
            // Do not flip such that the flipped board is a rotated version
            // of the old board, because the NN will not learn
            copy.flipPerspective();
            // Do not add the turn flag setting to the board notation
            return copy.getBoardNotation(false);
        }
    }

    public static double getScore(double[] prediction) {
        return prediction[0];
    }

    public static GnuMoves getBestMove(NeuralNetTwoHiddenTD nn,
                                       int[] diceRole, 
                                       GnuBoard gameBoard) {
        Color player = gameBoard.getTurn();
        Color otherPlayer = player == Color.WHITE ? Color.BLACK : Color.WHITE;
        List<GnuMoves> legalMoves = gameBoard.getLegalMoves(diceRole);
        if (legalMoves.isEmpty())
            return null;

        // Get the best move which results in the worst board for the opponent
        Double bestScore = null;
        GnuMoves bestMoves = null;
        for (GnuMoves moves : legalMoves) {
            // Copy constructor
            GnuBoard freshBoard = new GnuBoard(gameBoard);
            freshBoard.moveAndCheckGame(moves);
            double[] prediction = nn.predict(getBoardNotation(freshBoard, otherPlayer));
            // We want the worst score for the opponent
            double score = getScore(prediction);
            if (bestScore == null || score < bestScore) {
                bestScore = score;
                bestMoves = moves;
            }
        }

        assert bestMoves != null;
        // At times return a random move
        if (Math.random() < epsilon && legalMoves.size() > 1) {
            return legalMoves.get((int)(Math.random() * legalMoves.size()));
        }
        return bestMoves;
    }

    private double trainOnce(NeuralNetTwoHiddenTD nn) {
        GnuBoard board = new GnuBoard();
        board.setTurn(Math.random() >= 0.5 ? Color.WHITE : Color.BLACK);

        nn.traceZeros();

        while (true) {
            int[] diceRole = board.roleDice();
            // Do a move
            // Copy constructor
            GnuBoard beforeMoveBoard = new GnuBoard(board);
            GnuMoves bestMove = getBestMove(nn, diceRole, board);
            board.moveAndCheckGame(bestMove);
            if (board.isGameOver()) {
                double afterGamePrediction[] = new double[1];
                if (board.checkers[1][25] == 15)
                {
                    // Train the loser
                    afterGamePrediction[0] = 0.0;
                    nn.trainOne(getBoardNotation(beforeMoveBoard, board.getTurn()), afterGamePrediction);
                }
                else {
                    assert board.checkers[0][25] == 15;
                    // Train the winner
                    afterGamePrediction[0] = 1.0;
                    nn.trainOne(getBoardNotation(beforeMoveBoard, beforeMoveBoard.getTurn()), afterGamePrediction);
                }
                break;
            } else {
                // Use the same POV for both board notations so they differ just a small step
                double[] beforeMoveBoardNotation = getBoardNotation(beforeMoveBoard, beforeMoveBoard.getTurn());
                double[] afterMoveBoardNotation = getBoardNotation(board, beforeMoveBoard.getTurn());
                nn.trainOne(beforeMoveBoardNotation, nn.predict(afterMoveBoardNotation));
                beforeMoveBoardNotation = getBoardNotation(beforeMoveBoard, board.getTurn());
                afterMoveBoardNotation = getBoardNotation(board, board.getTurn());
                nn.trainOne(beforeMoveBoardNotation, nn.predict(afterMoveBoardNotation));
            }
        }

        return 0;
    }
}
