public class DriverDoNotUseTurnFlag {
    private static double epsilon = 0.09; // How often should we pick a random move?

    public static double[] getBoardNotation(GnuBoard gameBoard, Color player) {
        if (player == Color.WHITE) {
            // Do not add the turn flag setting to the board notation
            return gameBoard.getBoardNotation(false);
        }
        else {
            GnuBoard copy = new GnuBoard(gameBoard);
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
                                       GnuBoard gameBoard, 
                                       Color player) {
        Color otherPlayer = player == Color.WHITE ? Color.BLACK : Color.WHITE;
        List<GnuMoves> legalMoves = gameBoard.getLegalMoves(diceRole);
        if (legalMoves.isEmpty())
            return null;

        // Get the best move which results in the worst board for the opponent
        Double bestScore = null;
        GnuMoves bestMoves = null;
        for (GnuMoves moves : legalMoves) {
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
            Color player = board.getTurn();
            Color otherPlayer = player == Color.WHITE ? Color.BLACK : Color.WHITE;
            GnuBoard beforeMoveBoard = new GnuBoard(board);
            GnuMoves bestMove = getBestMove(nn, diceRole, board, player);
            board.moveAndCheckGame(bestMove);
            if (board.isGameOver()) {
                double afterGamePrediction[] = new double[1];
                // Assumes a GnuBG style board, 0 - 23, 24 for bar, 25 for off
                if (board.checkers[1][25] == 15)
                {
                    // Train the loser
                    assert player == Color.BLACK;
                    afterGamePrediction[0] = 0.0;
                    nn.trainOne(getBoardNotation(beforeMoveBoard, otherPlayer), afterGamePrediction);
                }
                else {
                    assert board.checkers[0][25] == 15;
                    assert player == Color.WHITE;
                    // Train the winner
                    afterGamePrediction[0] = 1.0;
                    nn.trainOne(getBoardNotation(beforeMoveBoard, player), afterGamePrediction);
                }
                break;
            } else {
                // Train both ways
                double[] nextState = nn.predict(getBoardNotation(board, player));
                nn.trainOne(getBoardNotation(beforeMoveBoard, player), nextState);
                nextState = nn.predict(getBoardNotation(board, otherPlayer));
                nn.trainOne(getBoardNotation(beforeMoveBoard, otherPlayer), nextState);
            }
        }

        return 0;
    }
}
