

package yeni;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SystemeDeVote {

    static boolean estConnecte = false;
    static String identifiantConnecte = ""; // Pour suivre l'utilisateur connecté
    static boolean aVote = false; // Pour vérifier si l'utilisateur a déjà voté

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            creerAdminParDefaut(); // Vérifie et crée un admin par défaut si nécessaire
            afficherPageAccueil(); // Affiche la page d'accueil
        });
    }

    // Création d'un administrateur par défaut
    static void creerAdminParDefaut() {
        try (Connection conn = Database.getConnection()) {
            // Vérifie si un administrateur existe déjà
            String checkAdminQuery = "SELECT * FROM utilisateurs WHERE role = 'admin'";
            try (PreparedStatement ps = conn.prepareStatement(checkAdminQuery)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return; // Un admin existe déjà, on ne fait rien
                }
            }

            // Si aucun admin n'existe, insère un administrateur par défaut
            String insertAdminQuery = "INSERT INTO utilisateurs (identifiant, mot_de_passe, role, age) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertAdminQuery)) {
                ps.setString(1, "admin");
                ps.setString(2, hashPassword("admin123")); // Mot de passe par défaut
                ps.setString(3, "admin"); // Rôle administrateur
                ps.setInt(4, 30); // Âge par défaut
                ps.executeUpdate();
                System.out.println("Varsayılan Yönetici başarıyla oluşturuldu.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Varsayılan yönetici oluşturulurken hata oluştu : " + e.getMessage());
        }
    }

    // Page d'accueil
    static void afficherPageAccueil() {
        JFrame frame = new JFrame("Elektronik Oylama Sistemi");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout()); // Utilisation de GridBagLayout pour centrer
        panel.setBackground(new Color(230, 230, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0); // Marges entre les composants
        gbc.gridx = 0; // Colonne unique
        gbc.weightx = 1; // Permet un alignement horizontal uniforme

        JButton btnConnexion = new JButton("Giriş yapmak");
        JButton btnInscription = new JButton("Kayıt olmak");
      
        JButton btnVoirNotifications = new JButton("Bildirimler");
        JButton btnVoirResultats = new JButton("Sonuçları görün");
        JButton btnQuitter = new JButton("Çıkış");

        styliserBouton(btnConnexion);
        styliserBouton(btnInscription);
     
        styliserBouton(btnVoirNotifications);
        styliserBouton(btnVoirResultats);
        styliserBouton(btnQuitter);

        btnConnexion.addActionListener(e -> connexion());
        btnInscription.addActionListener(e -> inscrire());

        btnVoirNotifications.addActionListener(e -> afficherNotifications());
        btnVoirResultats.addActionListener(e -> afficherResultats());
        btnQuitter.addActionListener(e -> quitter());

        // Ajout des boutons avec positionnement centré
        gbc.gridy = 0; // Ligne 0
        panel.add(btnConnexion, gbc);

        gbc.gridy = 1; // Ligne 1
        panel.add(btnInscription, gbc);

        gbc.gridy = 2; // Ligne 2
        panel.add(btnVoirNotifications, gbc);

        gbc.gridy = 3; // Ligne 3
        panel.add(btnVoirResultats, gbc);

        gbc.gridy = 4; // Ligne 4
        panel.add(btnQuitter, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    static void quitter() {
        System.exit(0);
    }

    static void afficherResultats() {
        try (Connection conn = Database.getConnection()) {
            String totalVotesQuery = "SELECT COUNT(*) AS total_votes FROM votes";
            String candidateVotesQuery = "SELECT candidat, COUNT(*) AS votes FROM votes GROUP BY candidat";

            int totalVotes = 0;

            try (PreparedStatement ps = conn.prepareStatement(totalVotesQuery)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalVotes = rs.getInt("total_votes");
                }
            }

            StringBuilder resultats = new StringBuilder("Oylama sonuçları:\n");
            try (PreparedStatement ps = conn.prepareStatement(candidateVotesQuery)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String candidat = rs.getString("candidat");
                    int votes = rs.getInt("votes");
                    double pourcentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0.0;
                    resultats.append(candidat).append(": ").append(votes).append(" kişi tarafından oylandı (")
                            .append(String.format("%.2f", pourcentage)).append("%)\n");
                }
            }

            JOptionPane.showMessageDialog(null, resultats.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Sonuçları görüntülerken hata oluştu : " + e.getMessage());
        }
    }
/*
 * 
 * loriginal 
    static void afficherNotifications() {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT identifiant_utilisateur, candidat FROM votes";
            StringBuilder notifications = new StringBuilder("Oy veren kullanıcılar :\n");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String utilisateur = rs.getString("identifiant_utilisateur");
                    String candidat = rs.getString("candidat");
                    notifications.append("- ").append(utilisateur).append(", ").append(candidat).append( " oy verdi " ).append("\n");
                }
            }

            JOptionPane.showMessageDialog(null, notifications.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Bildirim kurtarma hatası : " + e.getMessage());
        }
    }

    static void styliserBouton(JButton bouton) {
        bouton.setFont(new Font("Arial", Font.PLAIN, 14));
        bouton.setBackground(new Color(173, 216, 230));
        bouton.setFocusPainted(false);
    }
*/
    
    
   
    
    static void afficherNotifications() {
        try (Connection conn = Database.getConnection()) {
            // Inclure la date_vote dans la requête
            String query = "SELECT identifiant_utilisateur, candidat, date_vote FROM votes";
            StringBuilder notifications = new StringBuilder("Oy veren kullanıcılar :\n");

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String utilisateur = rs.getString("identifiant_utilisateur");
                    String candidat = rs.getString("candidat");
                    String dateVote = rs.getString("date_vote"); // Récupérer la date du vote

                    // Ajouter les informations au StringBuilder
                    notifications.append("- ")
                                 .append(utilisateur)
                                 .append(", ")
                                 .append(candidat)
                                 .append(" oy verdi ")
                                 .append("(Tarih: ")
                                 .append(dateVote)
                                 .append(")\n");
                }
            }

            // Afficher les notifications
            JOptionPane.showMessageDialog(null, notifications.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Bildirim kurtarma hatası : " + e.getMessage());
        }
    }

    
    
    
    static void styliserBouton(JButton bouton) {
        bouton.setFont(new Font("Arial", Font.PLAIN, 14));
        bouton.setBackground(new Color(173, 216, 230));
        bouton.setFocusPainted(false);
    }
    
    
    static void connexion() {
        String identifiant = JOptionPane.showInputDialog("Seçmeninizin kimliğini belirtin : ");
        String motDePasse = JOptionPane.showInputDialog("Parolanızı girin : ");

        if (identifiant == null || identifiant.trim().isEmpty() || motDePasse == null || motDePasse.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Lütfen tüm alanları doldurun !");
            return;
        }

        if (validerConnexion(identifiant, hashPassword(motDePasse))) {
            estConnecte = true;
            identifiantConnecte = identifiant;
            aVote = aDejaVote(identifiant);
            JOptionPane.showMessageDialog(null, "Bağlantı başarılı !");

            if (roleUtilisateur(identifiant).equals("admin")) {
                afficherPageAdmin(); // L'administrateur voit le menu de gestion des candidats
            } else {
                afficherInterfaceVote(); // Un utilisateur normal vote
            }
        } else {
            JOptionPane.showMessageDialog(null, "Yanlış kullanıcı adı veya şifre !");
        }
    }
    
    
    

    private static void afficherInterfaceVote() {
        // Vérifier si l'utilisateur a déjà voté
        if (aVote) {
            JOptionPane.showMessageDialog(null, "Zaten oy verdiniz !");
            return;
        }

        // Afficher l'interface de vote
        JFrame frame = new JFrame("Oylama Arayüzü");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // BoxLayout en Y pour les boutons verticaux

        JLabel label = new JLabel("Oy vermek için bir aday seçin :");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label); // Ajouter le label au début

        // Récupérer les candidats depuis la base de données
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT nom FROM candidats";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String candidat = rs.getString("nom");
                    JButton btnCandidat = new JButton(candidat);
                    btnCandidat.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrer chaque bouton

                    // Action à effectuer lors du clic sur un candidat
                    btnCandidat.addActionListener(e -> voter(candidat));
                    panel.add(btnCandidat);
                    
                    // Ajouter un espace après chaque bouton de candidat pour l'espacement
                    panel.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement entre les boutons
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Adaylar alınırken hata oluştu : " + e.getMessage());
        }

        // Ajouter un bouton "Retour"
        JButton btnRetour = new JButton("Geri");
        styliserBouton(btnRetour);
        btnRetour.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrer le bouton Retour
        btnRetour.addActionListener(e -> {
            frame.dispose();
            afficherPageAccueil();
        });

        // Ajouter un espace avant le bouton Retour
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Plus d'espace avant le bouton "Retour"
        panel.add(btnRetour);

        frame.add(panel);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre sur l'écran
        frame.setVisible(true);
    }



    static void voter(String candidat) {
        // Enregistrer le vote de l'utilisateur
        try (Connection conn = Database.getConnection()) {
            String insertVoteQuery = "INSERT INTO votes (identifiant_utilisateur, candidat) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertVoteQuery)) {
                ps.setString(1, identifiantConnecte); // Utilisateur connecté
                ps.setString(2, candidat); // Candidat choisi
                ps.executeUpdate();
                aVote = true; // Marquer l'utilisateur comme ayant voté

                JOptionPane.showMessageDialog(null, "" + candidat + " 'ya oy verdiniz: ");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Oy verirken hata : " + e.getMessage());
        }
    }

    


    
    
    
    static void afficherPageAdmin() {
        JFrame frame = new JFrame("Aday Yönetimi");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Création d'un panel avec GridBagLayout pour une disposition plus souple et symétrique
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Espacement autour des boutons

        // Création des boutons
        JButton btnAjouterCandidat = new JButton("Aday Ekle");
        JButton btnModifierCandidat = new JButton("Adayı Düzenle");
        JButton btnSupprimerCandidat = new JButton("Adayı Sil");
        JButton btnRetour = new JButton("Geri");

        // Application du style aux boutons
        styliserBouton(btnAjouterCandidat);
        styliserBouton(btnModifierCandidat);
        styliserBouton(btnSupprimerCandidat);
        styliserBouton(btnRetour);

        // Définir les actions des boutons
        btnAjouterCandidat.addActionListener(e -> ajouterCandidat());
        btnModifierCandidat.addActionListener(e -> modifierCandidat());
        btnSupprimerCandidat.addActionListener(e -> supprimerCandidat());
        btnRetour.addActionListener(e -> {
            frame.dispose();
            afficherPageAccueil(); // Retour à la page d'accueil après gestion
        });

        // Configuration du GridBagConstraints pour centrer et aligner les boutons
        gbc.gridx = 0;  // Position horizontale (1 colonne)
        gbc.gridy = 0;  // Première ligne
        gbc.gridwidth = 1; // Un bouton par ligne
        gbc.anchor = GridBagConstraints.CENTER;  // Centrer le bouton horizontalement
        panel.add(btnAjouterCandidat, gbc);

        gbc.gridy = 1;  // Ligne suivante pour le bouton suivant
        panel.add(btnModifierCandidat, gbc);

        gbc.gridy = 2;  // Ligne suivante
        panel.add(btnSupprimerCandidat, gbc);

        gbc.gridy = 3;  // Dernière ligne pour le bouton Retour
        panel.add(btnRetour, gbc);

        // Ajout du panel à la fenêtre
        frame.add(panel);
        frame.setLocationRelativeTo(null); // Centrer la fenêtre
        frame.setVisible(true);
    }


    static void ajouterCandidat() {
        String nomCandidat = JOptionPane.showInputDialog("Yeni adayın adı :");
        if (nomCandidat != null && !nomCandidat.trim().isEmpty()) {
            try (Connection conn = Database.getConnection()) {
                String query = "INSERT INTO candidats (nom) VALUES (?)";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, nomCandidat);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(null, "Aday başarıyla eklendi !");
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Aday eklenirken hata oluştu: " + e.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Aday adı boş olamaz.");
        }
    }

    static void modifierCandidat() {
        String nomCandidatAncien = JOptionPane.showInputDialog("Adayın şu anki adı :");
        if (nomCandidatAncien != null && !nomCandidatAncien.trim().isEmpty()) {
            String nomCandidatNouveau = JOptionPane.showInputDialog("Yeni aday adı :");
            if (nomCandidatNouveau != null && !nomCandidatNouveau.trim().isEmpty()) {
                try (Connection conn = Database.getConnection()) {
                    String query = "UPDATE candidats SET nom = ? WHERE nom = ?";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, nomCandidatNouveau);
                        ps.setString(2, nomCandidatAncien);
                        ps.executeUpdate();
                        JOptionPane.showMessageDialog(null, "Aday başarıyla değiştirildi !");
                    }
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Aday düzenlenirken hata oluştu : " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Aday adı bulunamadı.");
        }
    }

    static void supprimerCandidat() {
        String nomCandidat = JOptionPane.showInputDialog("Silinecek adayın adı:");
        if (nomCandidat != null && !nomCandidat.trim().isEmpty()) {
            // Vérifier si le candidat existe
            try (Connection conn = Database.getConnection()) {
                // Vérification des votes existants
                String checkVotesQuery = "SELECT COUNT(*) FROM votes WHERE candidat = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkVotesQuery)) {
                    psCheck.setString(1, nomCandidat);
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Si des votes existent pour ce candidat, réattribuer ou supprimer les votes
                        int option = JOptionPane.showConfirmDialog(null,
                                "Bu adayın oyları var. Oylarını yeniden atamak mı yoksa silmek mi istersiniz?",
                                "Oylar yeniden atandı.",
                                JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.YES_OPTION) {
                            // Réattribuer les votes à un autre candidat (exemple : à un candidat par défaut)
                            String nouveauCandidat = JOptionPane.showInputDialog("Lütfen yeni adayın adını girin:");
                            if (nouveauCandidat != null && !nouveauCandidat.trim().isEmpty()) {
                                String updateVotesQuery = "UPDATE votes SET candidat = ? WHERE candidat = ?";
                                try (PreparedStatement psUpdate = conn.prepareStatement(updateVotesQuery)) {
                                    psUpdate.setString(1, nouveauCandidat);
                                    psUpdate.setString(2, nomCandidat);
                                    psUpdate.executeUpdate();
                                    JOptionPane.showMessageDialog(null, "Oylar  yeniden  " + nouveauCandidat +  "atandı");
                                }
                            }
                        } else {
                            // Supprimer les votes associés à ce candidat
                            String deleteVotesQuery = "DELETE FROM votes WHERE candidat = ?";
                            try (PreparedStatement psDeleteVotes = conn.prepareStatement(deleteVotesQuery)) {
                                psDeleteVotes.setString(1, nomCandidat);
                                psDeleteVotes.executeUpdate();
                                JOptionPane.showMessageDialog(null, "Bu aday için tüm oylar silindi.");
                            }
                        }
                    }
                }

                // Supprimer le candidat de la base de données
                String query = "DELETE FROM candidats WHERE nom = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, nomCandidat);
                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Aday başarıyla silindi!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Bu isimle hiçbir aday bulunamadı.");
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "\r\n"
                		+ "Aday silme hatası:" + e.getMessage());
            }
        }
    }

     

    static boolean validerConnexion(String identifiant, String motDePasse) {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT * FROM utilisateurs WHERE identifiant = ? AND mot_de_passe = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, identifiant);
                ps.setString(2, motDePasse);
                ResultSet rs = ps.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Bağlantı hatası: " + e.getMessage());
        }
        return false;
    }

    static String roleUtilisateur(String identifiant) {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT role FROM utilisateurs WHERE identifiant = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, identifiant);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Rol alma hatası: " + e.getMessage());
        }
        return "";
    }

    static boolean aDejaVote(String identifiant) {
        try (Connection conn = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM votes WHERE identifiant_utilisateur = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, identifiant);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Retourne true si l'utilisateur a déjà voté
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Oy doğrulama hatası:" + e.getMessage());
        }
        return false;
    }

    static void inscrire() {
        // Afficher une fenêtre d'inscription
        JFrame frame = new JFrame("Kayıt");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(230, 230, 250));

        // Créer les champs d'entrée pour l'identifiant, le mot de passe et l'âge
        JTextField txtIdentifiant = new JTextField(20);
        JPasswordField txtMotDePasse = new JPasswordField(20);
        JTextField txtAge = new JTextField(20);

        panel.add(new JLabel("Kullanıcı Adı"));
        panel.add(txtIdentifiant);
        panel.add(new JLabel("Şifre"));
        panel.add(txtMotDePasse);
        panel.add(new JLabel("Yaş"));
        panel.add(txtAge);

        JButton btnInscrire = new JButton("Kayıt olmak");
        btnInscrire.addActionListener(e -> {
            String identifiant = txtIdentifiant.getText();
            String motDePasse = new String(txtMotDePasse.getPassword());
            String ageStr = txtAge.getText();

            if (identifiant.trim().isEmpty() || motDePasse.trim().isEmpty() || ageStr.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Lütfen tüm alanları doldurun.");
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                inscrireUtilisateur(identifiant, motDePasse, age);
                frame.dispose();
                JOptionPane.showMessageDialog(null, "Kayıt başarılı!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Yaş bir sayı olmalıdır.");
            }
        });

        panel.add(btnInscrire);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void inscrireUtilisateur(String identifiant, String motDePasse, int age) {
        try (Connection conn = Database.getConnection()) {
            String query = "INSERT INTO utilisateurs (identifiant, mot_de_passe, role, age) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, identifiant);
                ps.setString(2, hashPassword(motDePasse));
                ps.setString(3, "utilisateur");
                ps.setInt(4, age);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Kayıt sırasında hata:" + e.getMessage());
        }
    }

    static String hashPassword(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "Hashing hatası:" + e.getMessage());
        }
        return null;
    }
}