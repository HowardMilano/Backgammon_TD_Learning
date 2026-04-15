public class DriverUseTurnFlag {
    private static double epsilon = 0.09; // How often should we pick a random move?

    public double[] getBoardNotation(GnuBoard gameBoard) {
        // Add the turn flag setting in the board notation, 1.0 for WHITE at turn, 0.0 for BLACK at turn
        return gameBoard.getBoardNotation(true);
    }

    public double getScore(double[] prediction, Color mover, Color playerColor) {
        if (mover == playerColor) {
            return prediction[0];
        }
        else {
            return 1.0 - prediction[0];
        }
    }
    
    private GnuMoves getBestMove(NeuralNetTwoHiddenTD nn, int[] diceRole, GnuBoard gameBoard, Color playerColor) {
        List<GnuMoves> legalMoves = gameBoard.getLegalMoves(diceRole);
        if (legalMoves.isEmpty())
            return null;

        // Get the best move which results in the worst board for the opponent
        Double bestScore = null;
        GnuMoves bestMoves = null;
        for (GnuMoves moves : legalMoves) {
            GnuBoard freshBoard = new GnuBoard(gameBoard);
            freshBoard.moveAndCheckGame(moves);
            double[] prediction = nn.predict(getBoardNotation(freshBoard));
            // We want the worst winning score for the opponent
            double score = getScore(prediction, gameBoard.getTurn(), playerColor);
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

        // Player gets a random color
        Color playerColor = Math.random() >= 0.5 ? Color.WHITE : Color.BLACK;

        nn.traceZeros();

        while (true) {
            int[] diceRole = board.roleDice();
            // Do a move
            Color mover = board.getTurn();
            GnuBoard beforeMoveBoard = new GnuBoard(board);
            GnuMoves bestMove = getBestMove(nn, diceRole, board, playerColor);
            board.moveAndCheckGame(bestMove);
            if (board.isGameOver()) {
                double afterGamePrediction[] = new double[1];
                // A GnuBG style bord, 0 - 23, 24 for bar, 25 for off
                if (board.checkers[1][25] == 15)
                {
                    // Train the loser
                    assert mover == Color.BLACK;
                    afterGamePrediction[0] = 0.0;
                }
                else {
                    assert board.checkers[0][25] == 15;
                    assert mover == Color.WHITE;
                    // Did the player win? if (playerColor == mover) .....
                    // Train the winner
                    afterGamePrediction[0] = 1.0;
                }
                nn.trainOne(getBoardNotation(beforeMoveBoard), afterGamePrediction);
                break;
            } else {
                nn.trainOne(getBoardNotation(beforeMoveBoard), nn.predict(getBoardNotation(board)));
            }
        }

        return 0;
    }
}
