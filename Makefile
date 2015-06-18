test: local
	carton exec -- prove

cover: local
	carton exec -- cover -test -ignore_re \.t$$ -ignore_re local/


install: local

local: cpanfile
	carton install
	touch local


clean:
	rm -rf __repos

realclean: clean
	rm -rf local


.PHONY: install clean realclean test
