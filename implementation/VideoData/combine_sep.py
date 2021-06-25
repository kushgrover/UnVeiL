from moviepy.editor import VideoFileClip, concatenate_videoclips
import sys

dir = sys.argv[1]
dir_before = dir + "/before/"
dir_rrg = dir + "/rrg/"
dir_after = dir +"/after/"

num_before = int(sys.argv[2])
num_after = int(sys.argv[3])



# # Before
# k = int(num_before/200)
# for i in range(0, k):
#     clips = []
#     for j in range (i*200, (i+1)*200):
#         clips.append(VideoFileClip(dir_before + str(j) + ".mp4"))
#     final_clip = concatenate_videoclips(clips)
#     final_clip.write_videofile(dir_before + "before_slow_" + str(i) + ".mp4")
    
# clips = []
# for j in range (k*200, num_before+1):
#     clips.append(VideoFileClip(dir_before + str(j) + ".mp4"))
# final_clip = concatenate_videoclips(clips)
# final_clip.write_videofile(dir_before + "before_slow_" + str(k) + ".mp4")

# clips = []
# for i in range(0, k+1):
#     clips.append(VideoFileClip(dir_before + "before_slow_" + str(i) + ".mp4"))
# final_clip = concatenate_videoclips(clips)
# final_clip.write_videofile(dir + "/before_slow.mp4")





# # Before 10x faster
# from moviepy.editor import *
# clip = VideoFileClip(dir + "/before_slow.mp4")
# final_clip = clip.fx( vfx.speedx, 10)
# final_clip.write_videofile(dir + "/before.mp4")




# # RRG
# clips = []
# for i in range(0, 1):
#     clips.append(VideoFileClip(dir_rrg + "rrg.mp4"))
# final_clip = concatenate_videoclips(clips)
# final_clip.write_videofile(dir + "/rrg.mp4")




# # After
# clips = []
# for i in range(0, num_after+1):
#     clips.append(VideoFileClip(dir_after + str(i) + ".mp4"))
# final_clip = concatenate_videoclips(clips)
# final_clip.write_videofile(dir + "/after.mp4")


# Combine
clips = [VideoFileClip(dir + "/before.mp4"), VideoFileClip(dir + "/rrg.mp4"), VideoFileClip(dir + "/after.mp4")]
final_clip = concatenate_videoclips(clips)
final_clip.write_videofile("separate.mp4")