Parekh, Dhaval	UNI: dmp2179
Sarma, Akshai	UNI: as4107

Go to src folder. Place the CSV file here as usual.
javac heapManagement/*.java datatype/*.java

To Run the program and insert data from CSV file: 
		java heapManagement.Main my_program heap_file_path -i < example.csv
		
To Run the program and make any query:
		java heapManagement.Main my_program heap_file_path -s3 >= 1900 -s2 "<>" Jolie

P.S.: If you are using other operators besides =, surround them with double quotes like so: "<>".

Validation
-----------
We do basic validation such as incorrect format of options, such as -s[^0-9]* or providing unsupported
options. We basically return the same error message indicating that there is an error. We did
not add more validation as we felt it was not required. We do not do validations of the types of
the values in the options, providing incorrect values here causes the program to attempt to match
it at the byte level based on the schema in the heap header.

As for semantically wrong options such as providing -p10 when there are only 4 columns, these 
are essentially just ignored rather than providing an error message. Again, since there was
no specification with regard to this, we simply chose to do it the simpler way. The goal was
the program would not crash if provided an incorrect option.


