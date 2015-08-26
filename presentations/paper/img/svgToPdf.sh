#!/bin/bash

(
  echo imperative_subject.svg --export-pdf=imperative_subject.pdf;
  echo no_period_subject.svg --export-pdf=no_period_subject.pdf;
  echo no_short_message.svg --export-pdf=no_short_message.pdf;
  echo subject_limit.svg --export-pdf=subject_limit.pdf;
  echo body_used.svg --export-pdf=body_used.pdf;
) | DISPLAY= inkscape --shell
