
import java.text.DecimalFormat;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author asus
 */
public class AplikasiKeuanganPribadiGUI extends javax.swing.JFrame {

    private double totalPemasukan = 0;
    private double totalPengeluaran = 0;
    private double saldo = 0;
    private int selectedRow;

    /**
     * Creates new form AplikasiKeuanganPribadiGUI
     */
    public AplikasiKeuanganPribadiGUI() {
        initComponents();
        tampilkanTanggal();
        btnSave.addActionListener(evt -> saveTransactions());
        btnLoad.addActionListener(evt -> loadTransactions());

    }

    private void updateTotals() {
        totalPemasukan = 0;
        totalPengeluaran = 0;
        saldo = 0;

        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {
            double transaksi = (Double) model.getValueAt(i, 3);
            String kategori = (String) model.getValueAt(i, 1);

            if (kategori.equals("Gaji") || kategori.equals("Usaha") || kategori.equals("Hadiah") || kategori.contains("Pemasukan")) {
                totalPemasukan += transaksi;
                saldo += transaksi;
            } else {
                totalPengeluaran += transaksi;
                saldo -= transaksi;
            }
        }

        pemasukanLabel.setText("Pemasukan: Rp. " + totalPemasukan);
        pengeluaranLabel.setText("Pengeluaran: Rp. " + totalPengeluaran);
        totalLabel.setText("Saldo: Rp. " + saldo);
    }

    private void saveTransactions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("transactions.csv"))) {
            // Menulis header CSV
            writer.write("Tanggal,Kategori,Deskripsi,Transaksi");
            writer.newLine();

            DecimalFormat df = new DecimalFormat("#.##");  // Menggunakan format yang aman untuk angka

            // Menulis data dari JTable ke file CSV
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                String tanggal = (String) model.getValueAt(i, 0);
                String kategori = (String) model.getValueAt(i, 1);
                String deskripsi = (String) model.getValueAt(i, 2);
                double transaksi = (double) model.getValueAt(i, 3);

                String formattedTransaksi = df.format(transaksi);  // Format angka

                // Menulis baris data ke file
                writer.write(tanggal + "," + kategori + "," + deskripsi + "," + formattedTransaksi);
                writer.newLine();
            }

            // Menulis Total Pemasukan, Pengeluaran, dan Saldo
            writer.write("Total Pemasukan:," + df.format(totalPemasukan));
            writer.newLine();
            writer.write("Total Pengeluaran:," + df.format(totalPengeluaran));
            writer.newLine();
            writer.write("Saldo:," + df.format(saldo));
            writer.newLine();

            JOptionPane.showMessageDialog(this, "Data transaksi berhasil disimpan.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactions() {
        try (BufferedReader reader = new BufferedReader(new FileReader("transactions.csv"))) {
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0); // Kosongkan tabel sebelum memuat data baru

            // Skip header
            reader.readLine();

            String line;
            DecimalFormat df = new DecimalFormat("#.##");  // Format angka
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);  // Gunakan split dengan limit untuk data kosong
                if (data.length == 4) {
                    try {
                        String tanggal = data[0].trim();
                        String kategori = data[1].trim();
                        String deskripsi = data[2].trim();
                        double transaksi = Double.parseDouble(data[3].replace(",", "")); // Hilangkan koma
                        model.addRow(new Object[]{tanggal, kategori, deskripsi, df.format(transaksi)});
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Format data tidak valid: " + line, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (data.length == 2) {
                    // Baris untuk total
                    try {
                        String label = data[0].trim();
                        double value = Double.parseDouble(data[1].replace(",", "").trim());

                        // Perbarui total pemasukan, pengeluaran, atau saldo
                        updateTotalLabels(label, value);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Format data total tidak valid: " + line, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Data transaksi berhasil dimuat.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTotalLabels(String label, double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedValue = df.format(value);
        switch (label) {
            case "Total Pemasukan:":
                totalPemasukan = value;
                pemasukanLabel.setText("Pemasukan: Rp. " + formattedValue);
                break;
            case "Total Pengeluaran:":
                totalPengeluaran = value;
                pengeluaranLabel.setText("Pengeluaran: Rp. " + formattedValue);
                break;
            case "Saldo:":
                saldo = value;
                totalLabel.setText("Saldo: Rp. " + formattedValue);
                break;
            default:
                // Abaikan label yang tidak dikenal
                break;
        }
    }

    private void tampilkanTanggal() {
        java.util.Timer timer = new java.util.Timer();
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd-MM-yyyy HH:mm:ss");
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                labelTanggal.setText("Tanggal: " + now.format(formatter));
            }
        }, 0, 1000);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        transaksiField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        deskripsiFiedl = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        labelTanggal = new javax.swing.JLabel();
        cbKategori = new javax.swing.JComboBox<>();
        totalLabel = new javax.swing.JLabel();
        pemasukanLabel = new javax.swing.JLabel();
        pengeluaranLabel = new javax.swing.JLabel();
        btnTambah = new javax.swing.JButton();
        btnKeluar = new javax.swing.JButton();
        btnLoad = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel2.setText("Transaksi");

        jLabel3.setText("Deskripsi");

        jLabel4.setText("Kategori");

        labelTanggal.setText("Tanggal :");

        cbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Food", "Transportasi", "Rumah", "Pendidikan", "Kesehatan", "Hiburan", "Internet", "Gaji", "Usaha", "Hadiah", "Lain-lain Pemasukan", "Lain-lain Pengeluaran", " " }));

        totalLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        totalLabel.setText("Rp.");

        pemasukanLabel.setText("Pemasukan :");

        pengeluaranLabel.setText("Pengeluaran :");

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });
        btnTambah.addActionListener(e -> {
            // Logika untuk tambah data baru
            try {
                double transaksiUpdate = Double.parseDouble(transaksiField.getText());
                String deskripsiUpdate = deskripsiFiedl.getText();
                String kategoriUpdate = (String) cbKategori.getSelectedItem();

                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String waktuUpdate = java.time.LocalDateTime.now().format(formatter);

                if (kategoriUpdate == null || kategoriUpdate.isEmpty()) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Pilih kategori!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Tentukan apakah pemasukan atau pengeluaran
                if (kategoriUpdate.equals("Gaji") || kategoriUpdate.equals("Usaha") || kategoriUpdate.equals("Hadiah") || kategoriUpdate.contains("Pemasukan")) {
                    totalPemasukan += transaksiUpdate;
                    saldo += transaksiUpdate;
                } else {
                    totalPengeluaran += transaksiUpdate;
                    saldo -= transaksiUpdate;
                }

                // Tambahkan data ke tabel
                javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
                model.addRow(new Object[]{deskripsiUpdate, waktuUpdate, kategoriUpdate, transaksiUpdate, });

                // Perbarui label
                pemasukanLabel.setText("Pemasukan: Rp. " + totalPemasukan);
                pengeluaranLabel.setText("Pengeluaran: Rp. " + totalPengeluaran);
                totalLabel.setText("Saldo: Rp. " + saldo);

                // Kosongkan input field
                transaksiField.setText("");
                deskripsiFiedl.setText("");
                cbKategori.setSelectedIndex(0);

            } catch (NumberFormatException ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Masukkan jumlah transaksi yang valid!", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });

        btnKeluar.setText("Keluar");
        btnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKeluarActionPerformed(evt);
            }
        });

        btnLoad.setText("Load");

        btnSave.setText("Save");

        btnEdit.setText("Ubah");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnDelete.setText("Hapus");
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelTanggal)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel2)
                                            .addComponent(jLabel4))
                                        .addGap(30, 30, 30)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(deskripsiFiedl)
                                            .addComponent(cbKategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(transaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(pemasukanLabel)
                                    .addComponent(pengeluaranLabel)
                                    .addComponent(totalLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 457, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnTambah, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnKeluar, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(41, 41, 41))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(labelTanggal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(transaksiField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(deskripsiFiedl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addComponent(jLabel4))
                            .addComponent(cbKategori, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(totalLabel)
                        .addGap(18, 18, 18)
                        .addComponent(pemasukanLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pengeluaranLabel)))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTambah)
                    .addComponent(btnKeluar)
                    .addComponent(btnDelete)
                    .addComponent(btnEdit))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLoad)
                    .addComponent(btnSave))
                .addContainerGap())
        );

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 18)); // NOI18N
        jLabel1.setText("Keuangan Pribadi");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Deskripsi", "Time", "Kategori", "Value"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        btnExport.setText("Export");
        btnExport.addActionListener(e -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Ekspor Data ke Word");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Word Files", "docx"));

            int option = fileChooser.showSaveDialog(this);
            if (option == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();

                if (!filePath.endsWith(".docx")) {
                    filePath += ".docx";
                }

                try (org.apache.poi.xwpf.usermodel.XWPFDocument document = new org.apache.poi.xwpf.usermodel.XWPFDocument()) {
                    // Membuat tabel di dokumen Word
                    org.apache.poi.xwpf.usermodel.XWPFTable table = document.createTable();

                    // Menulis header ke tabel
                    org.apache.poi.xwpf.usermodel.XWPFTableRow headerRow = table.getRow(0);
                    headerRow.getCell(0).setText("Tanggal");
                    headerRow.addNewTableCell().setText("Kategori");
                    headerRow.addNewTableCell().setText("Deskripsi");
                    headerRow.addNewTableCell().setText("Transaksi");

                    // Menulis data dari tabel ke tabel Word
                    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
                    for (int i = 0; i < model.getRowCount(); i++) {
                        org.apache.poi.xwpf.usermodel.XWPFTableRow row = table.createRow();
                        for (int j = 0; j < model.getColumnCount(); j++) {
                            row.getCell(j).setText(model.getValueAt(i, j).toString());
                        }
                    }

                    // Menulis ke file
                    try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath)) {
                        document.write(fos);
                    }

                    javax.swing.JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke Word!", "Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } catch (java.io.IOException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Gagal menulis ke file Word: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnImport.setText("Import");
        btnImport.addActionListener(e -> {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Impor Data dari Excel");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx"));

            int option = fileChooser.showOpenDialog(this);
            if (option == javax.swing.JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();

                try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file)) {
                    // Membaca sheet pertama
                    org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

                    // Kosongkan tabel sebelum memuat data baru
                    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
                    model.setRowCount(0);

                    // Membaca data dari sheet ke tabel
                    for (org.apache.poi.ss.usermodel.Row row : sheet) {
                        if (row.getRowNum() == 0) continue; // Lewati header

                        // Membaca data dari setiap cell
                        String tanggal = "";
                        String kategori = "";
                        String deskripsi = "";
                        double transaksi = 0;

                        // Pastikan untuk mengecek apakah cell tersebut ada dan valid
                        if (row.getCell(0) != null) {
                            tanggal = row.getCell(0).getStringCellValue();
                        }
                        if (row.getCell(1) != null) {
                            kategori = row.getCell(1).getStringCellValue();
                        }
                        if (row.getCell(2) != null) {
                            deskripsi = row.getCell(2).getStringCellValue();
                        }
                        if (row.getCell(3) != null) {
                            transaksi = row.getCell(3).getNumericCellValue();
                        }

                        // Tambahkan data ke model tabel
                        model.addRow(new Object[]{tanggal, kategori, deskripsi, transaksi});
                    }

                    javax.swing.JOptionPane.showMessageDialog(this, "Data berhasil diimpor dari Excel!", "Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                } catch (java.io.IOException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Gagal memuat data dari file Excel: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnExport, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnImport, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExport)
                    .addComponent(btnImport))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKeluarActionPerformed
        System.exit(0);        // TODO add your handling code here:
    }//GEN-LAST:event_btnKeluarActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selectedRow = jTable1.getSelectedRow();

        if (selectedRow != -1) { // Periksa apakah ada baris yang dipilih
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin menghapus data ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                // Ambil kategori, deskripsi, dan transaksi dari baris yang akan dihapus
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                String kategori = model.getValueAt(selectedRow, 1).toString(); // Kolom kategori
                double transaksi = Double.parseDouble(model.getValueAt(selectedRow, 3).toString()); // Kolom transaksi

                // Hapus baris dari model tabel
                model.removeRow(selectedRow);

                // Perbarui total pemasukan, pengeluaran, dan saldo berdasarkan kategori
                if (kategori.equalsIgnoreCase("Pemasukan")) {
                    totalPemasukan -= transaksi; // Jika kategori pemasukan, kurangi dari total pemasukan
                } else if (kategori.equalsIgnoreCase("Pengeluaran")) {
                    totalPengeluaran -= transaksi; // Jika kategori pengeluaran, kurangi dari total pengeluaran
                    saldo += transaksi; // Tambah saldo karena pengeluaran dihapus
                }

                // Hitung ulang saldo
                saldo = totalPemasukan - totalPengeluaran;

                // Perbarui label di GUI
                pemasukanLabel.setText("Pemasukan: Rp. " + formatRupiah(totalPemasukan));
                pengeluaranLabel.setText("Pengeluaran: Rp. " + formatRupiah(totalPengeluaran));
                totalLabel.setText("Saldo: Rp. " + formatRupiah(saldo));

                JOptionPane.showMessageDialog(this, "Data berhasil dihapus.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin dihapus.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

// Fungsi untuk memformat angka menjadi format rupiah
    private String formatRupiah(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amount);


    }//GEN-LAST:event_btnDeleteActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jTable1MouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // Mendapatkan indeks baris yang dipilih
        int selectedRow = jTable1.getSelectedRow();

        if (selectedRow != -1) { // Periksa apakah ada baris yang dipilih
            try {
                // Mendapatkan data baru dari field input
                String kategori = cbKategori.getSelectedItem().toString();
                String deskripsi = deskripsiFiedl.getText();
                double transaksi = Double.parseDouble(transaksiField.getText());

                // Perbarui data di model tabel
                DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
                model.setValueAt(kategori, selectedRow, 1); // Kolom 1: Kategori
                model.setValueAt(deskripsi, selectedRow, 2); // Kolom 2: Deskripsi
                model.setValueAt(transaksi, selectedRow, 3); // Kolom 3: Transaksi

                JOptionPane.showMessageDialog(this, "Data berhasil diperbarui.");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Masukkan angka yang valid untuk transaksi.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit.", "Peringatan", JOptionPane.WARNING_MESSAGE);
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnEditActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AplikasiKeuanganPribadiGUI.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new AplikasiKeuanganPribadiGUI().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnKeluar;
    private javax.swing.JButton btnLoad;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cbKategori;
    private javax.swing.JTextField deskripsiFiedl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel labelTanggal;
    private javax.swing.JLabel pemasukanLabel;
    private javax.swing.JLabel pengeluaranLabel;
    private javax.swing.JLabel totalLabel;
    private javax.swing.JTextField transaksiField;
    // End of variables declaration//GEN-END:variables

}
