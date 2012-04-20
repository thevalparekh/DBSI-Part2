package heapManagement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CSVFileReader {
	private InputStream stream;
	private ArrayList <String> storeValues = new ArrayList<String>();
	
	public CSVFileReader(InputStream stream) {
		this.stream = stream;
	}
	
	public void ReadFile() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			String line;
			
			while( (line = br.readLine()) != null) {
				if(line.trim().equals("") || line.trim() == null ) {
					continue;
				}
				storeValues.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> getStoreValues() {
		return storeValues;
	}

	public void setStoreValues(ArrayList<String> storeValues) {
		this.storeValues = storeValues;
	}
		
}
