# Backgammon TD-Learning
With this document I want to help other people implement a basic version of a driver for TD-Learning for a Backgammon AI Bot. The code assumes there is lots of other code including a backgammon board class that defines the board and can make checkers move, and a neural network (NN) that is enabled for TD-Learning. From experience I can say that the code for the board and the NN is not easy but straightforward. The code that drives the learning of the NN so it can play backgammon is not at all obvious.

Modern AI agents can emit an NN that is TD-Learning enabled. They can also assist with creating the board class and the checker movement logic. However, none of the 3 AI agents I queried could write the code that drives the learning process. Sure they could write code that looked good at first glance but all the produced code failed to make an NN learn to play backgammon.

There are three drivers I researched. A driver that uses board notations that include a setting for the current turn (WHITE or BLACK), and a driver that uses notations without a turn setting but it uses flips of perspective (which Gnu Backgammon and other modern players use) and a third driver that uses both turn and flips perspective. The first driver is what many people will implement when they first write a backgammon bot. The second driver is trickier to implement and people will implement that after they get criticism that adding a turn setting in the board notation is not necessary. The third driver is, I think, an improvement over both.

The theory is that not including the turn setting, AND adding code to flip the board at appropriate times will make the NN use symmetry to learn to see one way of looking at the board, the who’s at turn POV.

In my testing, the third driver outperforms the other drivers.

The drivers can easily be extended to train the NN for 5 outputs (win, gammonWin, backgammonWin, gammonLoss, backgammonLoss) instead of the 1 output (win) the attached code uses.

If you really want to speed up the learning of the NN, start with a fresh NN and use supervised training (millions of positions) with the GnuBG training files (e.g. contact-train-data) or WildBG training files (e.g. wildBG_contact.csv). You do need to turn off TD-Learning in the NN while you do supervised training, then switch it on again after the training. That creates a beginner or intermediate player, depending on how much you train, and you can switch to TD-Learning after that. This speeds up learning from scratch by a 3X or 4X. You'll be amazed when you do that. To do this, use the third driver and for each GnuBG or WildBG position create your own board from that and set turn to BLACK so the POV matches the GnuBG POV then train.

To help with implementing code to train against GnuBG or WildBG position files, I added PositionDecoder.java and PositionEncoder.java which shows how to turn 20 or 14 character position strings into board positions or turn board positions into 14 character strings.

My current setup is 340 - 256 - 128 - 5 in the NN. I have tried 3 hidden layer NNs, but they tend to suffer learning collapses (forgets most learning) even when running at lambda 0.0. With a 2 hidden layer network, I've run one NN up to more than 1 million games at lambda 0.7, learning rate 0.001, totally stable. Many people recommend lambda 0.0, but I found that learning is slower and the results are definitely not better than with 0.7. I have also tried 340 - 512 - 245 - 5, but it was too big for my testing and research (read, it took too long).

I'm not done with researching using GnuBG or WildBG training files for supervised training AFTER TD-Learning is done. I've seen mixed results, it might be that the GnuBG training file are not good for this since their input to the NN is so much different than mine, and it might be that WildBG training files will work since their positions are based on rollouts.

Currently, my best backgammon player is, surprisingly, a 340 - 128 - 64 -4 NN which beats PubEval, the public reference player, about 66% of the time. My second best is a 340 - 256 - 128 - 5 NN player which beats PubEval also about 66% of the time but slightly less than my best player.

I use plain Java with just default Java libraries. I do use 1D arrays in my NN to speed up the loops. I parallelize the running of tests against a static NN, that makes it at least 5 times faster than running tests serially, well at least if your CPU has multiple cores. I use this code to run tests in parallel:

    IntStream.range(0, numberOfGames).parallel().forEach(gameNum -> { run test });

The attached code is how I implemented the drivers. I’m sure there are better ways and you can contact me (Howard@HowardMilano.com) with suggestions for improvements.

