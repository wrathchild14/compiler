JAVA		= java
ANTLR		= org.antlr.v4.Tool
ANTLRDIR	= ../../../lib/antlr-4.9.1-complete.jar
RM		= 'rm' -fr

parser	: synan/PinsParser.g4 lexan/PinsLexer.g4
	@# Lexer and parser must be constructed simultaneously.
	$(JAVA) -cp $(ANTLRDIR):$(CLASSPATH) $(ANTLR) -no-listener -no-visitor -lib lexan $^

lexer	: lexan/PinsLexer.g4
	@# Construction of lexer only.
	$(JAVA) -cp $(ANTLRDIR):$(CLASSPATH) $(ANTLR) $<

.PHONY	: clean
clean	:
	$(RM) *~
	$(RM) lexan/Pins*.java lexan/*interp lexan/*tokens
	$(RM) synan/Pins*.java synan/*interp synan/*tokens
