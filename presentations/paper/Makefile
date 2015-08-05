ssh-user = softlang@linux.uni-koblenz.de
www-dir = /home/softlang/www/docs
doc  = paper

build:	${doc}.pdf

upload: build
	scp -r -p ${doc}.pdf ${ssh-user}:${www-dir}/msr-paper.pdf

commit:
	rm -f ${doc}.pdf
	svn up ${doc}.pdf
	make build
	svn add ${doc}.pdf
	svn commit -m "Committed from Makefile"

%.pdf : %.tex %.bib Makefile
	pdflatex $<
	bibtex $*
	pdflatex $<
	pdflatex $<

ignore:
	svn propedit svn:ignore .

clean:
	rm -f *~
	rm -f *.aux
	rm -f *.log
	rm -f *.toc
	rm -f *.blg
	rm -f *.bbl
	rm -f ${doc}.ps
	rm -f ${doc}.pdf
	rm -f *.out
	rm -f *.out
	rm -f *.bak
	rm -f *stackdump*
