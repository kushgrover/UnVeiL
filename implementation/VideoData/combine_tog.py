from moviepy.editor import VideoFileClip, concatenate_videoclips
import sys

dir = sys.argv[1]
numIter = sys.argv[2]

clips = []
for i in range(-1, int(numIter)+1):
    clips.append(VideoFileClip(dir + "/" + str(i) + ".mp4"))
final_clip = concatenate_videoclips(clips)
final_clip.write_videofile("together.mp4")