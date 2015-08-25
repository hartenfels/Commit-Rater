# Commit-Rater

The best commit rating system of the multiverse.

## Useage
Either use the provided vagrant setup or manually install the dependencies. `make install` to install perl dependencies if needed (the vagrant setup does this automatically the first time).

Use carton to `exec` the Commit Rater perl script `commit-rater`:
``` Bash
carton exec perl commit-rater --remote=.
```

Remote may be any url of a Git repo. The command above runs the rater on it's own (this) repo.

## Dependencies
You should be able to run the Commit Rater on any Linux machine (Windows and Mac have not been tested), although we only testet it on Ubuntu. Below is a list of dependencies you'd have to install on Ubuntu.

### Commit Rater
* carton
* perl-doc
* git
* libaspell-dev

### Commit Rater Web
``` Bash
sudo apt-get install carton perl-doc git libssl-dev libaspell-dev nodejs nodejs-legacy npm
sudo apt-get install -g bower
```

## How we rate
What the fuck did you just fucking commit about me, you little git? I’ll have you know I graduated top of my course in PLT, and I’ve been involved in numerous secret merges on 101companies, and I have over 300 accepted pull requests. I am trained in extreme programming and I’m the top contributors in all of GitHub and BitBucket. You are nothing to me but just another build target. I will git rm -rf you the fuck out with permissions the likes of which has never been seen before on this WWW, tag my fucking commits. You think you can get away with saying that shit to me over the Internet? Think again, luser. As we speak I am contacting my secret network of bots across the Darknet and your IP is being traced right now so you better prepare for the merge conflicts, maggot. The merge that overwrites the pathetic little thing you call your project. You’re fucking removed, kid. I can commit anywhere, anytime, and I can bash you in under seven hundred lines, and that’s just with ed. Not only am I extensively trained in functional programming, but I have access to the entire arsenal of the ubuntu repos and I will use it to its full extent to fork bomb your miserable ass off the face of the contributors list, you little shit. If only you could have known what unholy contribution your little “clever” commit message was about to bring down upon you, maybe you would have held your fucking push. But you couldn’t, you didn’t, and now you’re paying the price, you goddamn idiot. I will shit blames all over you and you will drown in it. You’re fucking dead, kiddo.
