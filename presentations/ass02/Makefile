presentation.pdf: presentation.tex $(wildcard img/*)
	pdflatex presentation.tex
	pdflatex presentation.tex

clean:
	rm -f presentation.log presentation.aux presentation.nav \
	      presentation.out presentation.snm presentation.toc

realclean: clean
	rm -f presentation.pdf

.PHONY: clean realclean
