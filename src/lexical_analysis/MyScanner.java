package lexical_analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyScanner {
	BufferedReader br;
	StringBuffer buffer;
	int lineNum;
	public static final char EOF = '@';
	
	MyScanner(String filename){
		lineNum = 0;
		try{
			br = new BufferedReader(new FileReader(filename));
			buffer = new StringBuffer("");
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public int getLineNum(){
		return lineNum;
	}
	
	char readChar(){
		char result;
		if (buffer.length() == 0){
			try {
				if (br.ready()){
					buffer = new StringBuffer(br.readLine());
					lineNum += 1;
					return '\n';
				}
				else{
					return EOF;
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		result = buffer.charAt(0);
		buffer.deleteCharAt(0);
		return result;
	}
	
	void putBackChar(char c){
		buffer.insert(0,c);
	}
	
	char peekChar(){
		char result;
		if (buffer.length() == 0){
			try {
				if (br.ready()){
					buffer = new StringBuffer(br.readLine());
					lineNum+=1;
					return '\n';
				}
				else{
					return EOF;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		result = buffer.charAt(0);
//		buffer.deleteCharAt(0);
		return result;
	}
}