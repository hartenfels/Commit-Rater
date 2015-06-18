install: local

local: cpanfile
	carton install
	touch local


test: local
	carton exec -- prove


.PHONY: install test
