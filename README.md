<!--
*** Bintang Fajarianto
*** NIM 13519138
*** 27 Januari 2021
-->

<p align="center">
  <img src="https://ditsti.itb.ac.id/wp-content/uploads/2020/09/logo_itb_256.png" alt="Logo" width="100" height="100">
</p>

<h3 align="center"> Pemanfaatan Algoritma Greedy dalam
<br/> Aplikasi Permainan “Worms” </h3>

## Implementasi Algoritma Greedy

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Alternatif solusi <i>greedy</i> yang mungkin diterapkan pada permainan ini ada dua. Yakni, solusi <i>greedy</i> untuk mendapatkan score setinggi-tingginya dan mengulur <i>round</i> hingga menyentuh 400 atau solusi satu lagi adalah alternatif solusi untuk bisa secepat mematikan seluruh <i>Worms</i> lawan dan tetap membuat <i>Worms</i> player tersisa.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Alternatif pertama, memanfaatkan fungsi-fungsi dan method, baik bawaan maupun hasil implementasi sendiri, yang akan memberikan kemungkinan <i>Worms</i> untuk mengambil aksi-aksi yang menjauhkan dirinya dari musuh, dan sebanyak mungkin menghimpun <i>score</i> dengan cara melakukan <i>command dig</i> sebanyak mungkin.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
Pada alternatif kedua, solusi diimplementasikan dengan cara melakukan analisis terhadap kondisi kondisi yang ada selama permainan. Kondisi di sini meliputi <i>Worms</i> lawan maupun kawan, serta petak-petak tiap <i>cells</i> yang ada. Pada akhirnya, alternatif kedua akan membawa kondisi permainan kepada <i>Worms</i> player yang berhasil menghabisi <i>Worms</i> musuh sebelum <i>Worms</i> player habis.


## Prerequisites
Pastikan di laptop kamu telah terinstall <i>requirement</i> dasar berikut:
* [Java](https://www.oracle.com/java/technologies/javase/javasejdk8-downloads.html) (minimal Java 8)
* [IntellJ IDEA](https://www.jetbrains.com/idea/)
* [NodeJS](https://nodejs.org/en/download/)

Kebutuhan lainnya:
* [Aturan Worms](https://github.com/EntelectChallenge/2019-Worms/blob/develop/game-engine/game-rules.md)
* [Starter Pack](https://github.com/EntelectChallenge/2019-Worms/releases/tag/2019.3.2)
* [Visualizer](https://github.com/dlweatherhead/entelect-challenge-2019-visualiser/releases/tag/v1.0f1)

## How to Run Program
1. Pindah directory ke starter-pack (mengandung file make file)
  ```sh
  cd starter-pack
  ```

2. Run

* Windows
```sh
double click on run.bat
```


* Linux
  
jalankan perintah di terminal
  ```sh
  make run
  ```

Optional:

3. Pindahkan isi folder match-logs ke Matches pada folder Visualizer


4. Run Visualizer
* Windows
```sh
double click on start-visualiser
```

## Author
(1) &nbsp; Muhammad Rizal Muhaimin - [13519136](https://www.instagram.com/muhammadrizal.muhaimin/)

(2) &nbsp; Bintang Fajarianto - [13519138](https://www.instagram.com/bintangfrnz_/)

(3) &nbsp; Muhammad Akram Al Bari - [13519142](https://www.instagram.com/makram.bar/)