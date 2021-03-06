package com.orienit.kalyan.hadoop.training.cassandra;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.orienit.kalyan.hadoop.training.pdf.mapreduce.KalyanPdfInputFormat;
import com.orienit.kalyan.hadoop.training.pdf.mapreduce.KalyanPdfOutputFormat;

public class CassandraMRJob implements Tool {
	// Initializing configuration object
	private Configuration conf;

	@Override
	public Configuration getConf() {
		return conf; // getting the configuration
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf; // setting the configuration
	}

	@Override
	public int run(String[] args) throws Exception {

		// initializing the job configuration
		Job job = new Job(getConf());

		// setting the job name
		job.setJobName("Orien IT Cassandra MR Job");

		// to call this as a jar
		job.setJarByClass(this.getClass());

		// setting custom mapper class
		job.setMapperClass(CassandraMRMapper.class);

		// setting custom reducer class
		job.setReducerClass(CassandraMRReducer.class);

		// setting mapper output key class: K2
		job.setMapOutputKeyClass(Text.class);

		// setting mapper output value class: V2
		job.setMapOutputValueClass(LongWritable.class);

		// setting reducer output key class: K3
		job.setOutputKeyClass(Text.class);

		// setting reducer output value class: V3
		job.setOutputValueClass(LongWritable.class);

		// setting the input format class ,i.e for K1, V1
		job.setInputFormatClass(KalyanPdfInputFormat.class);

		// setting the output format class
		job.setOutputFormatClass(KalyanPdfOutputFormat.class);

		// setting the input file path
		FileInputFormat.addInputPath(job, new Path(args[0]));

		// setting the output folder path
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		Path outputpath = new Path(args[1]);
		// delete the output folder if exists
		outputpath.getFileSystem(conf).delete(outputpath, true);

		// to execute the job and return the status
		return job.waitForCompletion(true) ? 0 : -1;

	}

	public static void main(String[] args) throws Exception {
		int status = ToolRunner.run(new Configuration(), new CassandraMRJob(), args);
		System.out.println("My Status: " + status);
	}
}

class CassandraMRMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		// Read the line
		String line = value.toString();

		// Split the line into words
		String[] words = line.split(" ");

		// assign count(1) to each word
		for (String word : words) {
			context.write(new Text(word), new LongWritable(1));
		}
	}
}

class CassandraMRReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
	@Override
	protected void reduce(Text key, Iterable<LongWritable> values, Context context)
			throws IOException, InterruptedException {
		// Sum the list of values
		long sum = 0;
		for (LongWritable value : values) {
			sum = sum + value.get();
		}

		// assign sum to corresponding word
		context.write(key, new LongWritable(sum));
	}
}
