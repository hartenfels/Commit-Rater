test: local
	carton exec -- prove


install: local

local: cpanfile
	carton install
	touch local


clean:
	rm -rf __repos

realclean: clean
	rm -rf local


.PHONY: install clean realclean test
