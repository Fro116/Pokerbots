# OVERVIEW

This code was developed for MIT's annual pokerbots tournament http://mitpokerbots.com/. After winning the tournament in 2017, I decided to make my code public in hope that future competitors will find this resource useful.

The bot was trained using Counterfactual Regret Minimization (CFR). For more information about how training was done, see my CFR repository. This repo deals with the nitty gritty of implementing a CFR pokerbot, from abstracting the state space to dealing with off tree errors. 

# FILES

report.pdf: A brief strategy report that explains at a high level how the pokerbot is implemented. For a more detailed explanation about the design choices I made, message me at kundanc@mit.edu. I would love to hear from you!

helper/\*: Functions that use poker specific knowledge to precompute data. Deals with hand bucketing and discard logic.

player/javabot_1/\*: The actual implementation of the pokerbot. Deals with interpreting opponents moves and tracking the game state.

# INSTALLATION

The helper functions can be compiled using CMake. In order to actually run the pokerbot, one first needs a copy of the 2017 MIT pokerbots engine and a copy of the confix.txt file. To obtain them, try contacting pokerbots@mit.edu. Then, simply follow the instructions given in http://mitpokerbots.com/docs/.

# CONTACT

If you have any questions, feel free to contact me at kundanc@mit.edu


