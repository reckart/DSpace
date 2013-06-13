This Dspace version will probably work only with epic pid service (not tested what will happen if the PIDService setting are not added to config file)
In order to activate Epic PidService you have to add this lines to the dspace.cfg file, located in[dspace-install]/dspace/config/dspace.cfg), add it after line

# Default language for metadata values
default.language = en_US

pidservice.url = http://url.../ with a slash at the end! 
pidservice.user = username
pidservice.pass = password
pidservice.ver = pid service version 1 or 2

Successfully tested on localhast with demo pid service:
-creating a collection/Community
-uploading a file
