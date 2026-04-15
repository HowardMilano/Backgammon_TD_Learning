# Backgammon TD-Learning
With this document I want to help other people implement a basic version of a driver for TD-Learning for a Backgammon AI Bot. The code assumes there is lots of other code including a backgammon board class that defines the board and can make checkers move, and a neural network (NN) that is enabled for TD-Learning. From experience I can say that the code for the board and the NN is not easy but straightforward. The code that drives the learning of the NN so it can play backgammon is not at all obvious.

Modern AI agents can emit an NN that is TD-Learning enabled. They can also assist with creating the board class and the checker movement logic. However, none of the 3 AI agents I queried could write the code that drives the learning process. Sure they could write code that looked good at first glance but all the produced code failed to make an NN learn to play backgammon.

There are two drivers I researched. A driver that uses board notations that include a setting for the current turn (WHITE or BLACK), and a driver that uses notations without a turn setting (which Gnu Backgammon and other modern players use). The first driver is what many people will implement when they first write a backgammon bot. The second driver is trickier to implement and people will implement that after they get criticism that adding a turn setting in the board notation is not necessary.

The theory is that not including the turn setting, AND adding code to flip the board at appropriate times will make the NN use symmetry to learn to see one way of looking at the board, the who’s at turn POV.

After testing I realized that my code that doesn't store 'turn' doesn't learn as well as the code that does store 'turn'. I talked to Claude about it, and the reason is that code that doesn't store 'turn' is very tricky to implement, it even needs to keep track of forced skipped turns for instance. Any flipping or value adjusting that is done at the wrong time, will create a bot that will not learn as well as a bot that does store 'turn'. On top of that, it's not clear a bot that doesn't store 'turn' will perform much or any better than a bot that does store 'turn'. The advantage of the bot that does store 'turn' is that it's simple code that creates a very eager learner.

The driver for the bot that does store 'turn' can easily be extended to train the NN for 5 outputs (win, gammonWin, backgammonWin, gammonLoss, backgammonLoss) instead of the 1 output (win) the attached code uses.

The attached code is how I implemented the drivers. I’m sure there are better ways and you can contact me with suggestions for improvements.

