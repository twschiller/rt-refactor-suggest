export BIBINPUTS ?= .:bib

all: rt-refactoring-proposal.ps rt-refactoring-proposal.pdf

rt-refactoring-proposal.ps: rt-refactoring-proposal.dvi
	dvips $<

rt-refactoring-proposal.pdf: rt-refactoring-proposal.dvi
	dvipdfm $<

rt-refactoring-proposal.dvi: rt-refactoring-proposal.tex rt-refactoring-proposal.bib bib-update
	latex rt-refactoring-proposal.tex
	bibtex rt-refactoring-proposal
	latex rt-refactoring-proposal.tex
	latex rt-refactoring-proposal.tex

TAGS: tags
tags:
	etags `latex-process-inputs -list rt-refactoring-proposal.tex`

bib:
	hg clone https://plume-bib.googlecode.com/hg/ bib
.PHONY: bib-update
bib-update: bib
	cd bib && hg pull -u && make

