package Proiect_riw_1;

import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class Main {

	public static void main(String[] args) throws IOException{
		// TODO Auto-generated method stub
		Main MainObj = new Main();
		String cale = "D:\\Scoala\\workspace_riw\\Proiect_riw_1";
		String[] extension = new String[] { "html", "htm", "txt" };
		//String pathHtmlFiles = cale + "/getHtmlFiles";
		String pathHtmlFiles = cale + "/test-files";
		File director = new File(pathHtmlFiles);
		boolean Ok = true;
		List<File> filesFromDirectory = (List<File>) FileUtils.listFiles(director, extension, Ok);
		String stopWords = new Scanner(new File(cale + "/stopwords.txt"))
				.useDelimiter("\\Z").next();
		System.out.println("S-au citit stop wordurile de la calea : "+cale + "/stopwords.txt");
		 //array-urile sunt goale
		int indexFisier = 0;
		String finalOutput = cale + "/Rezultat/FinalOutput.txt";//rezultate finale
		for (File file : filesFromDirectory) {
			String pathFiles = cale + "/Rezultat/";
			String name1 = "In" + indexFisier;//generare nume fisier de intrare
			String name2 = "Output" + indexFisier;//generare nume fisier iesire
			File intrare = new File(pathFiles + name1 + ".txt");//generare fisier input
			File iesire = new File(pathFiles + name2 + ".txt");//generare fisier output

			PrintWriter writer1 = new PrintWriter(pathFiles + name1 + ".txt");//obiect pentru scriere in fisier input
			PrintWriter writer2 = new PrintWriter(pathFiles + name2 + ".txt");//obiect pentru scriere in fisier output 
			
			MainObj.putDataInText(file, writer1);//citirea datelor din fisierele html si scrierea intr-un txt de intrare
			MainObj.readDataFromFile(intrare, writer2, file,cale, stopWords);//citire cuvinte din fisier + generare index <document, cuvant>
			
			PrintWriter output = new PrintWriter(finalOutput);//generare fisier final de iesire
			MainObj.WordsCounting(iesire, output);//Numarare aparitie cuvinte
			indexFisier++;
		}
		File fileIDE = new File(finalOutput);
		getIDF(fileIDE, 25);
		// afisez cuvintele cu idf calculat
		showIDF();
		//scan.close();
	}
	//afisarea idf pe consola
	private static void showIDF() {
		for (Entry<String, Double> entry : tableIDE.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue().toString());
		}

	}
	//afisare index indirect <cuvant, fisier, numar_aparitii>
	public void afisareIndex(String file)
	{
		BufferedReader reader;
		try {
		reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		while(line != null)
		{
			System.out.println(line);
			line = reader.readLine();
		}
		reader.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	//citirea datelor din fisierele text si eliminarea stopwordurilor
	public void readDataFromFile(File file, PrintWriter output, File files,String cale, String stopWords) throws FileNotFoundException {
		HashMap<String, Integer> tabel = new HashMap<String, Integer>();
		//@SuppressWarnings("resource")
		try {
			Scanner scan = new Scanner(file);
			String words;
			while (scan.hasNext()) {
				words = scan.next();
				words = words.toLowerCase();

				if (words.contains("\"") || words.contains(",") || words.contains(".") || words.contains("!")
						|| words.contains("?") || words.contains("/")) {
					words = words.replace(",", "");
				}

				if (stopWords.contains(words)) {
					words = "";
				}

				if (words != "") {
					String canonicWords = getCanonicForm(words);

					if (tabel.containsKey(canonicWords)) {
						tabel.put(canonicWords, tabel.get(canonicWords) + 1);
					} else {
						tabel.put(canonicWords, 1);
					}
				}
			}
			scan.close();
			afisare(output, tabel, files);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// afisare cuvinte din directoare
	public void afisare(PrintWriter writer, HashMap<String, Integer> tabel, File file) {
		for (Map.Entry<String, Integer> entry : tabel.entrySet()) {
			writer.write(file.toString() + " ");
			writer.write(entry.getKey() + " ");
			writer.write(entry.getValue().toString());
			writer.write('\n');
		}
	}
	// aflam forma canonica din alg porter
	public String getCanonicForm(String word) {
		PorterAlgorithm porterAlg = new PorterAlgorithm();
		String p1 = porterAlg.step1(word);
		String p2 = porterAlg.step2(p1);
		String p3 = porterAlg.step3(p2);
		String p4 = porterAlg.step4(p3);
		String p5 = porterAlg.step5(p4);
		return p5;
	}

	//citirea datelor din fisierele html si punere in fisiere txt
	public void putDataInText(File file, PrintWriter writer) {
		try {
			// laborator1
			Document doc = Jsoup.parse(file, "UTF-8");
			String title = doc.title();
			String text = doc.body().text();
			// keywords
			String keywords = doc.head().select("meta[name=keywords]").attr("content");
			// desc
				String description = doc.head().select("meta[name=description]").attr("content");			
			// luam toate linkurile din pagina
			//org.jsoup.select.Elements links = doc.getElementsByTag("a");
			writer.write(title);
			writer.write('\n');
			writer.write(keywords);
			writer.write('\n');
			writer.write(description);
			writer.write('\n');

			writer.write(text);
			writer.write('\n');

			writer.close();

		} catch (IOException e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	//extragere cuvinte din fisier text
	private static HashMap<String, String> finalTable = new HashMap<String, String>();

	// fol pt a afisa nr de aparitii ale cuvantului
	public void WordsCounting(File file, PrintWriter output) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;

		while ((line = br.readLine()) != null) {
			String[] words = line.split(" ");
			String text = words[0] + " : " + words[2] + " ";
			if (finalTable.containsKey(words[1])) {
				finalTable.put(words[1], finalTable.get(words[1]) + text);
			} else {
				finalTable.put(words[1], text);
			}
		}

		showWords(output, finalTable);
		output.close();
		br.close();
	}

	// afisez pe consola
	private static void showWords(PrintWriter output, HashMap<String, String> tableFinal2) {
		for (Entry<String, String> entry : tableFinal2.entrySet()) {
			output.write(entry.getKey());
			output.write(" ");
			output.write(entry.getValue().toString());
			output.write('\n');
		}

	}

	static HashMap<String, Double> tableIDE = new HashMap<String, Double>();

	//calculare idf
	//pentru cuvintele care apar in toate fisierele html idf = 0
	public static void getIDF(File file, int nr_fisiere) throws IOException {

		FileReader fileReader = new FileReader(file);

		BufferedReader br = new BufferedReader(fileReader);

		String line = null;
		// daca nu mai sunt linii va returna null
		while ((line = br.readLine()) != null) {
			// citeste pana la EOF
			System.out.println(line);
			String[] words = line.split(" ");
			// cuvantul e primul
			String cuvant = words[0];
			int x = (words.length / 2);
			double rez = 0.0;
			if (nr_fisiere % x == 0) {
				if (nr_fisiere / x != 0)
					rez = Math.log(nr_fisiere / x);
				else
					rez = 0.0;
			} else {
				if (nr_fisiere / x != 0)
					rez = Math.log(nr_fisiere / (x - 1));
				else
					rez = 0.0;
			}

			if (!tableIDE.containsKey(cuvant)) {
				tableIDE.put(cuvant, rez);
			}
		}
		br.close();
	}
	//not used yet
	//distanta cosinus intre un document si query
	private double calculateScore(List<Double> queryWeights, List<Double> documentWeights) {
        double upper = 0;
        double lower = 0;
        double queryWeightsLength = 0;
        double documentWeightsLength = 0;

        for (int i = 0; i < queryWeights.size(); ++i) {
            upper += queryWeights.get(i) * documentWeights.get(i);
            queryWeightsLength += queryWeights.get(i) * queryWeights.get(i);
            documentWeightsLength += documentWeights.get(i) * documentWeights.get(i);
        }

        queryWeightsLength = Math.sqrt(queryWeightsLength);
        documentWeightsLength = Math.sqrt(documentWeightsLength);
        lower = queryWeightsLength * documentWeightsLength;

        return upper / lower;
    }

}
