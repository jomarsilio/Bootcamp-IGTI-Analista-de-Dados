
package IGTI;

import java.io.*;
import java.util.*;
import java.util.Random;
import java.text.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class ExemploIGTI extends Configured implements Tool 
{
    public static void main (final String[] args) throws Exception {   
        int res = ToolRunner.run(new Configuration(), new ExemploIGTI(), args);        
        System.exit(res);           
    }   

    public int run (final String[] args) throws Exception {
        try {
            // Inicializa o job.
            JobConf conf = new JobConf(getConf(), ExemploIGTI.class);
            // Define o nome do job a ser executado.
            conf.setJobName("Calculo Covid19");

            final FileSystem fs = FileSystem.get(conf);

            // Define o nome do diretorio e entrada e saida.
            Path diretorioEntrada = new Path("PastaEntrada"), diretorioSaida = new Path("PastaSaida");
            // Cria o diretorio de entrada. O diretorio de saida é criado automaticamente pelo Hadoop.
            fs.mkdirs(diretorioEntrada);

            // Copia o dataset para o diretorio de entrada.
            fs.copyFromLocalFile(new Path("/usr/local/hadoop/Dados/covidData.txt"), diretorioEntrada);

            // Configura o diretorio de entrada e saida.
            FileInputFormat.setInputPaths(conf, diretorioEntrada);
            FileOutputFormat.setOutputPath(conf, diretorioSaida);

            // Seta o tipo da chave e valor.
            conf.setOutputKeyClass(Text.class);
            conf.setOutputValueClass(Text.class);

            // Define as classes que contem o map e o reduce.
            conf.setMapperClass(MapIGTI.class);
            conf.setReducerClass(ReduceIGTI.class);

            // Executa o job.
            JobClient.runJob(conf);            
          }
          catch ( Exception e ) {
              throw e;
          }

          return 0;
     }
 
    public static class MapIGTI extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)  throws IOException {
            // Declara os objetos.
            Text txtChave = new Text();
            Text txtValue = new Text();
            
            // Gera um vetor (array).
            String[] dadosCovid = value.toString().split(",");
            
            // Extrai a data e o pais.
            String dataEvento = dadosCovid[0];
            String paisEvento = dadosCovid[2];

            // Extrai o total de casos e obitos.
            int novosCasos = Integer.parseInt(dadosCovid[4]);
            int novosObitos = Integer.parseInt(dadosCovid[6]);

            // Define o valor da chave e o valor.
            String strChave = dataEvento;
            String strValor = paisEvento + "|" + String.valueOf(novosCasos) + "|" + String.valueOf(novosObitos);

            // Seta a chave e o valor.
            txtChave.set(strChave);
            txtValue.set(strValor);

            // Envia a chave e valor para o HDFS.
            output.collect(txtChave, txtValue);
        }
    }
 
    public static class ReduceIGTI extends MapReduceBase implements Reducer<Text, Text, Text, Text> {       
        public void reduce (Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
            // Inicializa as variaveis.
            int maiorCasos = 0, maiorObitos = 0;
            String paisCasos = "", paisObitos = "", strSaida = "";
            Text value = new Text();

            // Define o objeto que recebera os valores do map.
            String[] campos = new String[3];

            while (values.hasNext()) {
                // Resgata o valor da linha da iteracao.
                value = values.next();

                // Popula o vetor.
                campos = value.toString().split("\\|");

                if (Integer.parseInt(campos[1]) > maiorCasos) {
                    // Seta o valor do maior.
                    maiorCasos = Integer.parseInt(campos[1]);
                    paisCasos = campos[0];
                }

                if (Integer.parseInt(campos[2]) > maiorObitos) {
                    // Seta o valor do maior.
                    maiorObitos = Integer.parseInt(campos[2]);
                    paisObitos = campos[0];
                }
            }

            // Define o valor da saida.
            strSaida = "Casos: " + String.valueOf(maiorCasos) + " em " + paisCasos + ".";
            strSaida += "Obitos: " + String.valueOf(maiorObitos) + " em " + paisObitos + ".";

            // Seta o valor gerado na linha da iteracao.
            value.set(strSaida);
            
            // Envia a chave e valor para o HDFS.
            output.collect(key, value);
        }
    }

    public static class MapIGTIMaior extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter)  throws IOException {
            //   
        }
    }

    public static class ReduceIGTIMaior extends MapReduceBase implements Reducer<Text, Text, Text, Text> {   
        public void reduce (Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {      
            //
        }
    }
}
