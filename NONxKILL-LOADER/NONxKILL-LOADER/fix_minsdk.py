from pathlib import Path

p = Path("app/build.gradle")
txt = p.read_text()

txt = txt.replace("minSdkVersion 21", "minSdkVersion 24")
txt = txt.replace("minSdk 21", "minSdk 24")

p.write_text(txt)

print("minSdk fixed")
