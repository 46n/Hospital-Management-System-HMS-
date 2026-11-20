"""Render the README walkthrough with Python and Matplotlib."""
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

rows = [["01","Check the request","Patient ID, doctor ID, date and time","Reject incomplete input or a past date."],["02","Resolve both people","Look up the patient and doctor","Stop if either record is missing."],["03","Check the calendar","Match the shift and both schedules","Reject an off-shift time or a conflict."],["04","Save the booking","Create a confirmed appointment","Attach the IDs and persist the record."]]
fig, ax = plt.subplots(figsize=(10, 5.7))
fig.subplots_adjust(left=0, right=1, top=1, bottom=0)
ax.set(xlim=(0, 10), ylim=(0, 5.7))
ax.axis("off")
fig.patch.set_facecolor("#10263b")
ax.text(.45, 5.15, "How a booking becomes a record", fontsize=22, weight="bold", color="white")
ax.text(.46, 4.77, "AppointmentManager.book   /   Java source walkthrough", fontsize=11, color="#bdd8df")
for index, (label, heading, method, meaning) in enumerate(rows):
    y = 3.63 - index * 1.05
    ax.add_patch(FancyBboxPatch((.45, y), 9.1, .86, boxstyle="round,pad=.04", facecolor="#edf5f6", linewidth=0))
    ax.text(.68, y+.42, label, va="center", weight="bold", color="#00776d", fontsize=13)
    ax.text(1.58, y+.42, heading, va="center", weight="bold", color="#173047", fontsize=14)
    ax.text(5.15, y+.54, method, color="#173047", fontsize=11)
    ax.text(5.15, y+.22, meaning, color="#496278", fontsize=10)
fig.savefig(Path(__file__).with_name("walkthrough.png"), dpi=160)
