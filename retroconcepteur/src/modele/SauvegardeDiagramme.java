package modele;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import vue.BlocClasse;
import vue.LiaisonVue;

/**
 * Sauvegarde lisible du diagramme (positions et liaisons) au format JSON.
 * Inclut les multiplicités, rôles, offsets et propriétés UML.
 */
public class SauvegardeDiagramme {
    public static void exporter(List<BlocClasse> blocs, List<LiaisonVue> liaisons, File fichier)
            throws Exception {
        if (fichier.getParentFile() != null)
            fichier.getParentFile().mkdirs();

        try (FileWriter w = new FileWriter(fichier)) {
            w.write("{\n");
            
            // Export des Blocs (Classes)
            w.write("  \"blocs\": [\n");
            for (int i = 0; i < blocs.size(); i++) {
                BlocClasse b = blocs.get(i);
                w.write(String.format(
                        "    {\"nom\": \"%s\", \"x\": %d, \"y\": %d, \"largeur\": %d, \"hauteur\": %d}%s\n",
                        escape(b.getNom()), b.getX(), b.getY(), b.getLargeur(), b.getHauteur(),
                        (i < blocs.size() - 1 ? "," : "")));
            }
            w.write("  ],\n");

            // Export des Liaisons (avec rôles, offsets et propriétés)
            w.write("  \"liaisons\": [\n");
            for (int i = 0; i < liaisons.size(); i++) {
                LiaisonVue l = liaisons.get(i);
                w.write(String.format(
                        "    {\"orig\": \"%s\", \"dest\": \"%s\", \"type\": \"%s\", " +
                        "\"multOrig\": \"%s\", \"multDest\": \"%s\", " +
                        "\"roleOrig\": \"%s\", \"roleDest\": \"%s\", " +
                        "\"roleOrigOffsetAlong\": %d, \"roleOrigOffsetPerp\": %d, " +
                        "\"roleDestOffsetAlong\": %d, \"roleDestOffsetPerp\": %d, " +
                        "\"proprietes\": \"%s\", \"contrainte\": \"%s\"}%s\n",
                        escape(l.getNomClasseOrig()), escape(l.getNomClasseDest()), l.getType().name(),
                        escape(l.getMultipliciteOrig()), escape(l.getMultipliciteDest()),
                        escape(l.getRoleOrig()), escape(l.getRoleDest()),
                        l.getRoleOrigOffsetAlong(), l.getRoleOrigOffsetPerp(),
                        l.getRoleDestOffsetAlong(), l.getRoleDestOffsetPerp(),
                        escape(l.getProprietes()), escape(l.getContrainte()),
                        (i < liaisons.size() - 1 ? "," : "")));
            }
            w.write("  ]\n");
            w.write("}\n");
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}