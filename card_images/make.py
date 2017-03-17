import sys, re
import os
import subprocess

input_dir = sys.argv[1]
output_dir = sys.argv[2]

for root, dirs, files in os.walk(input_dir):
    for f in filter(lambda a: len(a) > 3 and a[-4:] == ".svg", files):
        #contents = open(os.path.join(root, f)).read()
        #contents = re.sub("width=\".*?\"", "width=\"350mm\"", contents, 1)
        #contents = re.sub("height=\".*?\"", "height=\"180mm\"", contents, 1)
        #contents = re.sub("viewBox=\".*?\"", "viewbox=\"400 1000 2600 1800\"", contents, 1)
        #with open(os.path.join(root, f), "w") as newf:
        #    newf.write(contents)

        subprocess.check_output(["svg2png", "--width", "7000", "--height", "3600", os.path.join(root, f)])
   
