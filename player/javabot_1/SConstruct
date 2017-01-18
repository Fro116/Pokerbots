import os
# On Windows, the path is incorrect, so change the path to
# the environment one. Even though windows is the problem,
# do it for all platforms
env = Environment(JARCHDIR='classes')
newpath=os.environ.get('PATH')
env.Append(ENV = { 'PATH' : newpath })

env.Java(target='classes', source='src')
env.Jar(target='pokerbot.jar', source=['classes', 'Manifest.txt'])
