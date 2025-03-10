package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_NOT_SERIAL_NUMBER = "売上ファイル名が連番になっていません";
	private static final String OVER_10_DESITS = "合計⾦額が10桁を超えました合計⾦額が10桁を超えました";
	private static final String NOT_EXIST_NAME = "の支店コードが不正です";
	private static final String INVALID_FORMAT = "のフォーマットが不正です";
	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

//		エラー処理3-1
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		File[] files = new File(args[0]).listFiles();
		List<File> rcdFiles = new ArrayList<File>();

		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();

			if (files[i].isFile() && fileName.matches("^[0-9]{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}
//		エラー処理2-1
		Collections.sort(rcdFiles);
		for (int i = 0; i < rcdFiles.size() - 1; i++) {
			int fileNumber = Integer.parseInt(rcdFiles.get(i).getName().substring(0,8));
			int nextFileNumber = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0,8));

			if (nextFileNumber - fileNumber != 1) {
				System.out.println(FILE_NOT_SERIAL_NUMBER);
				return;
			}
		}


		for (int i = 0; i < rcdFiles.size(); i++) {
			BufferedReader br = null;

			try {
				FileReader fr = new FileReader(rcdFiles.get(i));
				br = new BufferedReader(fr);
				String line;
				String currentFileName = rcdFiles.get(i).getName();
				List<String> rcdContents = new ArrayList<String>();

				while ((line = br.readLine()) != null) {
					rcdContents.add(line);
				}
//				エラー処理2-4
				if (rcdContents.size() != 2) {
					System.out.println(currentFileName + INVALID_FORMAT);
					return;
				}
//				エラー処理2-3
				if (!branchSales.containsKey(rcdContents.get(0))) {
					System.out.println(currentFileName + NOT_EXIST_NAME);
					return;
				}
//				エラー処理3-2
				if (!rcdContents.get(1).matches("^[0-9]*$")) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				long fileSale = Long.parseLong(rcdContents.get(1));
				long sumSale = branchSales.get(rcdContents.get(0)) + fileSale;
//				エラー処理2-2
				if (sumSale >= 10000000000L) {
					System.out.println(OVER_10_DESITS);
					return;
				}

				branchSales.replace(rcdContents.get(0), sumSale);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}

			}
		}





		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
//			エラー処理1-1
			if (!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line;

			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] codeName = line.split(",");
//				エラー処理2-1
				if((codeName.length != 2) || (!codeName[0].matches("^[0-9]{3}$"))){
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				branchNames.put(codeName[0], codeName[1]);
				branchSales.put(codeName[0], 0L);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)
		BufferedWriter bw = null;

		try {
			File resultFile = new File(path, fileName);
			FileWriter fw = new FileWriter(resultFile);
			bw = new BufferedWriter(fw);

			for (String key : branchNames.keySet()) {
				String outName = branchNames.get(key);
				long outPrice = branchSales.get(key);

				bw.write(key + "," + outName + "," + outPrice);
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}
}
