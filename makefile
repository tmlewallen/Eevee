all: 
	javac src/*/*.java -d bin

source-arrays: testPrograms/arrays.ev
	cat testPrograms/arrays.ev

arrays: all
	./eevee testPrograms/arrays.ev

source-conditionals: testPrograms/conditionals.ev
	cat testPrograms/conditionals.ev

conditionals: all
	./eevee testPrograms/conditionals.ev

source-recursion: testPrograms/recursion.ev
	cat testPrograms/recursion.ev

recursion: all
	./eevee rtestPrograms/ecursion.ev

source-iteration: testPrograms/iteration.ev
	cat testPrograms/iteration.ev

iteration: all
	./eevee testPrograms/iteration.ev

source-functions: testPrograms/functions.ev
	cat testPrograms/functions.ev

functions: all
	./eevee testPrograms/functions.ev

source-lists: testPrograms/lists.ev
	cat testPrograms/lists.ev

lists: all
	./eevee testPrograms/lists.ev

clean:
	rm -r bin/*