echo off
Rem 2017-07-10 Übertragen einiger CapellaDateien auf virtuelles Telefon (AVD) zum Testen von CapAlyser
Rem Zusätzlich werden auch PDF-Dateien zum Testen übertragen

Rem Für Umlaute muss 
Rem 1) die BatDat muss auf 'Ansi' eingestellt sein.
Rem 2) :
chcp 1252
Rem 3) Die Anzeige im cmd ist trotzdem faslch, aber Dateien sind lesbar.

set adb="c:\Users\TyE\AppData\Local\Android\sdk\platform-tools\adb.exe"
set win=D:\TyE\Clouds\Dropbox\SVM-Intern\in Arbeit\CapAlyser\MusterDaten\
set avdCap=/mnt/sdcard/SVM
set avdPDF=/mnt/sdcard/SVM/PDF
set avdLEER=/mnt/sdcard/SVM/LEER

Echo Enable Read/Write ...
%adb% shell "su -c mount -o rw,remount rootfs /"
%adb% shell "su -c chmod 777 /mnt/sdcard"
%adb% shell mkdir "%avdLEER%"

Echo Copying Files
CALL :push Jahresprogramm 2016.zip
CALL :push Jahresprogramm 2017.zip
CALL :push Standard Geistliche Lieder.zip
CALL :push Standard Kunstlieder.zip
CALL :push Standard Volkslieder.zip
CALL :push Standard Volkslieder mit Jodler.zip


:eof
pause
EXIT /B 0

:push
Rem Hier erfolgt die Übertragunf auf Android:
%adb% push "%win%\%*"	"%avdCap%/%*"
EXIT /B 0
