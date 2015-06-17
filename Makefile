install: cpanfile
	carton install
	touch local

local: install


test: local
	carton exec -- prove


.PHONY: install test
