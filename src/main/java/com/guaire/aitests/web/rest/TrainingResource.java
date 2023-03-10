package com.guaire.aitests.web.rest;

import com.guaire.aitests.domain.Message;
import com.guaire.aitests.domain.Training;
import com.guaire.aitests.domain.Word;
import com.guaire.aitests.repository.MessageRepository;
import com.guaire.aitests.repository.TrainingRepository;
import com.guaire.aitests.repository.WordRepository;
import com.guaire.aitests.web.rest.errors.BadRequestAlertException;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.NeuralNetworkType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.guaire.aitests.domain.Training}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class TrainingResource {

    private final Logger log = LoggerFactory.getLogger(TrainingResource.class);

    private static final String ENTITY_NAME = "training";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TrainingRepository trainingRepository;

    private final MessageRepository messageRepository;

    private final WordRepository wordRepository;

    private final int aIInputSize = 100;
    private final int aiOutputSize = 2;

    public TrainingResource(TrainingRepository trainingRepository, MessageRepository messageRepository, WordRepository wordRepository) {
        this.trainingRepository = trainingRepository;
        this.messageRepository = messageRepository;
        this.wordRepository = wordRepository;
    }

    /**
     * {@code POST  /trainings} : Create a new training.
     *
     * @param training the training to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new training, or with status {@code 400 (Bad Request)} if the training has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/trainings")
    public ResponseEntity<Training> createTraining(@Valid @RequestBody Training training) throws URISyntaxException, IOException {
        log.debug("REST request to save Training : {}", training);
        if (training.getId() != null) {
            throw new BadRequestAlertException("A new training cannot already have an ID", ENTITY_NAME, "idexists");
        }

        String line = "";
        String bias = "";
        if (training.getIsLeft()) {
            bias = "left";
        } else {
            bias = "right";
        }

        byte[] trainingFile = training.getTwitterFeedFile();
        byte[] wordsFile = training.getAiFile();

        InputStream inputStream = null;
        BufferedReader buffReader = null;

        if (wordsFile != null) {
            inputStream = new ByteArrayInputStream(wordsFile);
            buffReader = new BufferedReader(new InputStreamReader(inputStream));

            while ((line = buffReader.readLine()) != null) {
                System.out.println(line);

                if (!line.isBlank() && !line.isEmpty()) {
                    if (!line.contains("http") || !line.isEmpty() || !line.isBlank()) {
                        saveWord(cleanWord(line));
                    }
                }
            }
            training.setAiFile(null);
        }

        inputStream = new ByteArrayInputStream(trainingFile);
        buffReader = new BufferedReader(new InputStreamReader(inputStream));
        line = "";
        List<Word> allWords = wordRepository.findAll();
        for (Word word : allWords) {
            word.setValue(0d);
        }
        int conti = 0;
        while ((line = buffReader.readLine()) != null) {
            System.out.println(line);

            if (!line.isBlank() && !line.isEmpty()) {
                conti++;
                System.out.println("Saving... " + conti);
                Message message = new Message();
                message.setText(line);
                message.setManualBias(bias);
                messageRepository.save(message);
            }

            String[] splitLine = line.split(" ");

            for (Word dictionaryWord : allWords) {
                for (String sentenceWord : splitLine) {
                    if (dictionaryWord.getName().equals(cleanWord(sentenceWord))) {
                        dictionaryWord.setValue(dictionaryWord.getValue() + 1);
                    }
                }
            }
        }

        Collections.sort(
            allWords,
            new Comparator<Word>() {
                public int compare(Word o1, Word o2) {
                    // compare two instance of `Score` and return `int` as result.
                    int result = 0;
                    if (o1.getValue() < o2.getValue()) {
                        result = 1;
                    } else if (o1.getValue() > o2.getValue()) {
                        result = -1;
                    } else {
                        result = 0;
                    }

                    return result;
                }
            }
        );

        double wordCount = 1d;
        for (Word dictionaryWord : allWords) {
            dictionaryWord.setValue(wordCount++);
            wordRepository.save(dictionaryWord);
        }

        training.setStatus("pre-training");
        Training result = trainingRepository.save(training);
        return ResponseEntity
            .created(new URI("/api/trainings/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    public ResponseEntity<Training> trainDiscriminator(Training train) throws IOException, URISyntaxException {
        System.out.println("Beginning training...");

        int inputSize = aIInputSize;
        int outputSize = aiOutputSize;

        NeuralNetwork ann = new NeuralNetwork();

        Layer inputLayer = new Layer();
        for (int i = 0; i < inputSize; i++) {
            inputLayer.addNeuron(new Neuron());
        }

        Layer hiddenLayerOne = new Layer();
        for (int i = 0; i < inputSize / 2; i++) {
            hiddenLayerOne.addNeuron(new Neuron());
        }

        Layer hiddenLayerTwo = new Layer();
        for (int i = 0; i < inputSize / 3; i++) {
            hiddenLayerTwo.addNeuron(new Neuron());
        }

        Layer hiddenLayerThree = new Layer();
        for (int i = 0; i <= outputSize * 4; i++) {
            hiddenLayerThree.addNeuron(new Neuron());
        }

        Layer hiddenLayerFour = new Layer();
        for (int i = 0; i < outputSize * 3; i++) {
            hiddenLayerFour.addNeuron(new Neuron());
        }

        Layer hiddenLayerFive = new Layer();
        for (int i = 0; i < outputSize * 2; i++) {
            hiddenLayerFive.addNeuron(new Neuron());
        }

        Layer outputLayer = new Layer();
        for (int i = 0; i < outputSize; i++) {
            outputLayer.addNeuron(new Neuron());
        }

        ann.addLayer(0, inputLayer);
        ann.addLayer(1, hiddenLayerOne);
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(1));
        ann.addLayer(2, hiddenLayerTwo);
        ConnectionFactory.fullConnect(ann.getLayerAt(1), ann.getLayerAt(2));
        ann.addLayer(3, hiddenLayerThree);
        ConnectionFactory.fullConnect(ann.getLayerAt(2), ann.getLayerAt(3));
        ann.addLayer(4, hiddenLayerFour);
        ConnectionFactory.fullConnect(ann.getLayerAt(3), ann.getLayerAt(4));
        ann.addLayer(5, hiddenLayerFive);
        ConnectionFactory.fullConnect(ann.getLayerAt(4), ann.getLayerAt(5));
        ann.addLayer(6, outputLayer);
        ConnectionFactory.fullConnect(ann.getLayerAt(5), ann.getLayerAt(6));
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(ann.getLayersCount() - 1), false);

        ann.setInputNeurons(inputLayer.getNeurons());
        ann.setOutputNeurons(outputLayer.getNeurons());

        ann.setNetworkType(NeuralNetworkType.MULTI_LAYER_PERCEPTRON);

        System.out.println("Created ANN...");

        DataSet ds = new DataSet(inputSize, outputSize);

        List<Message> messages = messageRepository.findAll();
        Collections.shuffle(messages);

        List<Word> allWords = wordRepository.findAll();

        List<String> fakeSentences = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            fakeSentences.add(generateRandomSentence());
        }

        int cont2 = 0;

        for (String fakeSentence : fakeSentences) {
            double input[] = new double[inputSize];
            double result[] = new double[outputSize];
            System.out.println("Transforming fake sentence " + cont2++ + " of " + fakeSentences.size());
            String[] words = fakeSentence.split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                if (numberValue != 0d) {
                    addedInfo = true;
                }

                input[cont] = numberValue;
                cont++;
            }

            result[0] = 0;
            result[1] = 0;

            if (addedInfo) {
                ds.addRow(input, result);
            }
        }

        cont2 = 0;

        for (Message message : messages) {
            double input[] = new double[inputSize];
            double result[] = new double[outputSize];
            System.out.println("Transforming sentence " + cont2++ + " of " + messages.size());
            String[] words = message.getText().split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                if (numberValue != 0d) {
                    addedInfo = true;
                }

                input[cont] = numberValue;
                cont++;
            }

            result[0] = 1;
            result[1] = 1;

            if (addedInfo) {
                ds.addRow(input, result);
            }
        }

        //fill input and output here
        cont2 = 1;
        for (Message message : messages) {
            double input[] = new double[inputSize];
            double result[] = new double[outputSize];
            System.out.println("Transforming sentence " + cont2++ + " of " + messages.size());
            String[] words = message.getText().split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                if (numberValue != 0d) {
                    addedInfo = true;
                }

                input[cont] = numberValue;
                cont++;
            }

            if (message.getManualBias().equals("left")) {
                result[0] = 1;
                result[1] = 0;
            } else {
                result[0] = 0;
                result[1] = 1;
            }

            if (addedInfo) {
                ds.addRow(input, result);
            }
        }

        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setMaxIterations(11111);

        ann.setLearningRule(backPropagation);

        System.out.println("Starting training...");

        ann.learn(ds);

        ann.save("DiscriminatorNet.nnet");

        File file = new File("DiscriminatorNet.nnet");
        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        train.setTwitterFeedFile(bytesArray);
        train.setTwitterFeedFileContentType("text/plain");
        train.setStatus("trained!");

        Training trainingResult = trainingRepository.save(train);

        System.out.println("Training successful...");

        return ResponseEntity
            .created(new URI("/api/trainings/" + trainingResult.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, trainingResult.getId().toString()))
            .body(trainingResult);
    }

    public ResponseEntity<Training> discriminatorImprover(Training train) throws IOException, URISyntaxException {
        System.out.println("Beginning discriminator improver training...");

        File file2 = new File("DiscriminatorNet.nnet");

        FileOutputStream fos2 = new FileOutputStream(file2);
        fos2.write(train.getAiFile()); //write files into byte
        fos2.close();

        NeuralNetwork ann2 = NeuralNetwork.createFromFile("DiscriminatorNet.nnet");

        int inputSize = aIInputSize;
        int outputSize = aiOutputSize;

        NeuralNetwork ann = new NeuralNetwork();

        Layer inputLayer = new Layer();
        for (int i = 0; i < inputSize; i++) {
            inputLayer.addNeuron(new Neuron());
        }

        Layer hiddenLayerOne = new Layer();
        for (int i = 0; i < inputSize / 2; i++) {
            hiddenLayerOne.addNeuron(new Neuron());
        }

        Layer hiddenLayerTwo = new Layer();
        for (int i = 0; i < inputSize / 3; i++) {
            hiddenLayerTwo.addNeuron(new Neuron());
        }

        Layer hiddenLayerThree = new Layer();
        for (int i = 0; i <= outputSize * 4; i++) {
            hiddenLayerThree.addNeuron(new Neuron());
        }

        Layer hiddenLayerFour = new Layer();
        for (int i = 0; i < outputSize * 3; i++) {
            hiddenLayerFour.addNeuron(new Neuron());
        }

        Layer hiddenLayerFive = new Layer();
        for (int i = 0; i < outputSize * 2; i++) {
            hiddenLayerFive.addNeuron(new Neuron());
        }

        Layer outputLayer = new Layer();
        for (int i = 0; i < outputSize; i++) {
            outputLayer.addNeuron(new Neuron());
        }

        ann.addLayer(0, inputLayer);
        ann.addLayer(1, hiddenLayerOne);
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(1));
        ann.addLayer(2, hiddenLayerTwo);
        ConnectionFactory.fullConnect(ann.getLayerAt(1), ann.getLayerAt(2));
        ann.addLayer(3, hiddenLayerThree);
        ConnectionFactory.fullConnect(ann.getLayerAt(2), ann.getLayerAt(3));
        ann.addLayer(4, hiddenLayerFour);
        ConnectionFactory.fullConnect(ann.getLayerAt(3), ann.getLayerAt(4));
        ann.addLayer(5, hiddenLayerFive);
        ConnectionFactory.fullConnect(ann.getLayerAt(4), ann.getLayerAt(5));
        ann.addLayer(6, outputLayer);
        ConnectionFactory.fullConnect(ann.getLayerAt(5), ann.getLayerAt(6));
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(ann.getLayersCount() - 1), false);

        ann.setInputNeurons(inputLayer.getNeurons());
        ann.setOutputNeurons(outputLayer.getNeurons());

        ann.setNetworkType(NeuralNetworkType.MULTI_LAYER_PERCEPTRON);

        System.out.println("Created ANN...");

        DataSet ds = new DataSet(inputSize, outputSize);

        List<Message> messages = messageRepository.findAll();
        Collections.shuffle(messages);

        List<Word> allWords = wordRepository.findAll();

        List<String> fakeSentences = new ArrayList<>();
        int counter2 = 0;
        int realWordCounter = 0;
        do {
            String fakeSentence = generateRandomSentence();

            double input[] = new double[inputSize];

            System.out.println("Generating fake sentence " + realWordCounter + " of " + 600);
            String[] words = fakeSentence.split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                input[cont++] = numberValue;
            }

            ann2.setInput(input);
            ann2.calculate();
            double output[] = ann2.getOutput();
            if (output[0] == 1d && output[1] == 1d) {
                fakeSentences.add(fakeSentence);
                realWordCounter++;
                double result[] = new double[outputSize];
                result[0] = 0;
                result[1] = 0;
                ds.addRow(input, result);
            }
        } while (realWordCounter < 600);

        int cont2 = 0;

        for (Message message : messages) {
            double input[] = new double[inputSize];
            double result[] = new double[outputSize];
            System.out.println("Transforming sentence " + cont2++ + " of " + messages.size());
            String[] words = message.getText().split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                if (numberValue != 0d) {
                    addedInfo = true;
                }

                input[cont] = numberValue;
                cont++;
            }

            result[0] = 1;
            result[1] = 1;

            if (addedInfo) {
                ds.addRow(input, result);
            }
        }

        //fill input and output here
        cont2 = 1;
        for (Message message : messages) {
            double input[] = new double[inputSize];
            double result[] = new double[outputSize];
            System.out.println("Transforming sentence " + cont2++ + " of " + messages.size());
            String[] words = message.getText().split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                if (numberValue != 0d) {
                    addedInfo = true;
                }

                input[cont] = numberValue;
                cont++;
            }

            if (message.getManualBias().equals("left")) {
                result[0] = 1;
                result[1] = 0;
            } else {
                result[0] = 0;
                result[1] = 1;
            }

            if (addedInfo) {
                ds.addRow(input, result);
            }
        }

        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setMaxIterations(111);

        ann.setLearningRule(backPropagation);

        System.out.println("Starting training...");

        ann.learn(ds);

        ann.save("DiscriminatorNet.nnet");

        File file = new File("DiscriminatorNet.nnet");
        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        train.setTwitterFeedFile(bytesArray);
        train.setTwitterFeedFileContentType("text/plain");
        train.setStatus("trained!");

        Training trainingResult = trainingRepository.save(train);

        System.out.println("Training successful...");

        return ResponseEntity
            .created(new URI("/api/trainings/" + trainingResult.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, trainingResult.getId().toString()))
            .body(trainingResult);
    }

    private String cleanWord(String word) {
        word = word.replace("\"", "");
        word = word.replace(".", "");
        word = word.replace(",", "");
        word = word.replace(";", "");
        word = word.replace("???", "");
        word = word.replace("???", "");
        word = word.replace("'s", "");
        word = word.toLowerCase();
        return word;
    }

    private void saveWord(String word) {
        List<Word> words = wordRepository.findByName_IgnoreCase(word);
        if (words == null || words.size() == 0) {
            Double maxNumberForRefference = (double) wordRepository.count() + 1;

            Word oWord = new Word();
            oWord.setName(cleanWord(word));
            oWord.setValue(maxNumberForRefference);
            wordRepository.save(oWord);
        }
    }

    @GetMapping("/trainings/do-training")
    public ResponseEntity<Training> train() throws IOException, URISyntaxException {
        System.out.println("Beginning training...");

        int inputSize = aIInputSize;
        int outputSize = aiOutputSize;

        NeuralNetwork ann = new NeuralNetwork();

        Layer inputLayer = new Layer();
        for (int i = 0; i < inputSize; i++) {
            inputLayer.addNeuron(new Neuron());
        }

        Layer hiddenLayerOne = new Layer();
        for (int i = 0; i < inputSize / 2; i++) {
            hiddenLayerOne.addNeuron(new Neuron());
        }

        Layer hiddenLayerTwo = new Layer();
        for (int i = 0; i < inputSize / 3; i++) {
            hiddenLayerTwo.addNeuron(new Neuron());
        }

        Layer hiddenLayerThree = new Layer();
        for (int i = 0; i <= outputSize * 4; i++) {
            hiddenLayerThree.addNeuron(new Neuron());
        }

        Layer hiddenLayerFour = new Layer();
        for (int i = 0; i < outputSize * 3; i++) {
            hiddenLayerFour.addNeuron(new Neuron());
        }

        Layer hiddenLayerFive = new Layer();
        for (int i = 0; i < outputSize * 2; i++) {
            hiddenLayerFive.addNeuron(new Neuron());
        }

        Layer outputLayer = new Layer();
        for (int i = 0; i < outputSize; i++) {
            outputLayer.addNeuron(new Neuron());
        }

        ann.addLayer(0, inputLayer);
        ann.addLayer(1, hiddenLayerOne);
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(1));
        ann.addLayer(2, hiddenLayerTwo);
        ConnectionFactory.fullConnect(ann.getLayerAt(1), ann.getLayerAt(2));
        ann.addLayer(3, hiddenLayerThree);
        ConnectionFactory.fullConnect(ann.getLayerAt(2), ann.getLayerAt(3));
        ann.addLayer(4, hiddenLayerFour);
        ConnectionFactory.fullConnect(ann.getLayerAt(3), ann.getLayerAt(4));
        ann.addLayer(5, hiddenLayerFive);
        ConnectionFactory.fullConnect(ann.getLayerAt(4), ann.getLayerAt(5));
        ann.addLayer(6, outputLayer);
        ConnectionFactory.fullConnect(ann.getLayerAt(5), ann.getLayerAt(6));
        ConnectionFactory.fullConnect(ann.getLayerAt(0), ann.getLayerAt(ann.getLayersCount() - 1), false);

        ann.setInputNeurons(inputLayer.getNeurons());
        ann.setOutputNeurons(outputLayer.getNeurons());

        ann.setNetworkType(NeuralNetworkType.MULTI_LAYER_PERCEPTRON);

        System.out.println("Created ANN...");

        DataSet ds = new DataSet(inputSize, outputSize);

        List<Message> messages = messageRepository.findAll();
        Collections.shuffle(messages);

        List<Word> allWords = wordRepository.findAll();

        //fill input and output here
        int cont2 = 1;
        for (Message message : messages) {
            double input[] = new double[inputSize];
            double result[] = new double[outputSize];
            System.out.println("Transforming sentence " + cont2++ + " of " + messages.size());
            String[] words = message.getText().split(" ");
            int cont = 0;
            boolean addedInfo = false;
            for (String word : words) {
                if (cont >= aIInputSize - 1) {
                    break;
                }

                Double numberValue = 0d;
                try {
                    numberValue = getWordNumber(word, allWords);
                } catch (Exception ex) {
                    System.out.println("Skipping word " + word);
                }

                if (numberValue != 0d) {
                    addedInfo = true;
                }

                input[cont] = numberValue;
                cont++;
            }

            if (message.getManualBias().equals("left")) {
                result[0] = 1;
                result[1] = 0;
            } else {
                result[0] = 0;
                result[1] = 1;
            }

            if (addedInfo) {
                ds.addRow(input, result);
            }
        }

        BackPropagation backPropagation = new BackPropagation();
        backPropagation.setMaxIterations(1111);

        ann.setLearningRule(backPropagation);

        System.out.println("Starting training...");

        ann.learn(ds);

        ann.save("Net.nnet");

        File file = new File("Net.nnet");
        //init array with file length
        byte[] bytesArray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        Training training = new Training();
        training.aiFile(bytesArray);
        training.aiFileContentType("text/plain");
        training.setTwitterFeedFile(bytesArray);
        training.setTwitterFeedFileContentType("text/plain");
        training.setStatus("trained!");

        Training trainingResult = trainingRepository.save(training);

        System.out.println("Training successful...");

        trainDiscriminator(training);

        discriminatorImprover(training);

        return ResponseEntity
            .created(new URI("/api/trainings/" + trainingResult.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, trainingResult.getId().toString()))
            .body(trainingResult);
    }

    public double getWordNumber(String word, List<Word> words) {
        for (Word oWord : words) {
            if (oWord.getName().equals(cleanWord(word))) {
                return oWord.getValue();
            }
        }
        return 0d;
    }

    @GetMapping("/messages/generate")
    public ResponseEntity<Message> generate() throws IOException, URISyntaxException {
        Training training = trainingRepository.getByStatus("trained!");

        File file = new File("Net.nnet");

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(training.getAiFile()); //write files into byte
        fos.close();

        NeuralNetwork ann = NeuralNetwork.createFromFile("Net.nnet");

        File file2 = new File("DiscriminatorNet.nnet");

        FileOutputStream fos2 = new FileOutputStream(file2);
        fos2.write(training.getAiFile()); //write files into byte
        fos2.close();

        NeuralNetwork ann2 = NeuralNetwork.createFromFile("DiscriminatorNet.nnet");

        //fill info here
        double input[] = new double[aIInputSize];
        Random rand = new Random();
        int sentenceLength = rand.nextInt(50);
        int cont = 0;

        boolean goodSentenceFound = false;
        String leftOrRight = "";
        do {
            //creating a random sentence using numbers
            for (int i = 0; i < sentenceLength; i++) {
                int test = rand.nextInt(5);
                if (test == 1) {
                    input[i] = rand.nextInt((int) (wordRepository.count() - 1)); //all words
                } else if (test == 2) {
                    input[i] = rand.nextInt((int) (200 - 1)); //common words
                } else {
                    input[i] = rand.nextInt((int) (100 - 1)); //more common words
                }
            }

            ann.setInput(input);
            ann.calculate();
            double[] networkOutputOne = ann.getOutput();

            System.out.println(networkOutputOne[0] + " " + networkOutputOne[1]);

            if (networkOutputOne[0] == 1d) {
                leftOrRight = "left ";
            }
            if (networkOutputOne[1] == 1d) {
                leftOrRight = "right";
            }
            if (networkOutputOne[0] == 0d && networkOutputOne[1] == 0d) {
                leftOrRight = "neutral";
            }

            ann2.setInput(input);
            ann2.calculate();
            double[] networkOutputTwo = ann2.getOutput();

            if (networkOutputTwo[0] == 1d && networkOutputTwo[1] == 1d) {
                goodSentenceFound = true;
            }
            sentenceLength = rand.nextInt(50);
        } while (!goodSentenceFound);

        String sentenceWords = "";
        for (int i = 0; i < sentenceLength; i++) {
            Word word = wordRepository.getByValue(input[i]);
            if (word != null) {
                sentenceWords += word.getName() + " ";
            }
        }

        Message result = new Message();
        result.setText(sentenceWords);
        result.setaIBias(leftOrRight);
        result.setId(1L);

        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    public String generateRandomSentence() throws IOException, URISyntaxException {
        Training training = trainingRepository.getByStatus("trained!");

        File file = new File("Net.nnet");

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(training.getAiFile()); //write files into byte
        fos.close();

        NeuralNetwork ann = NeuralNetwork.createFromFile("Net.nnet");

        //fill info here
        double input[] = new double[aIInputSize];
        Random rand = new Random();
        int sentenceLength = rand.nextInt(25);
        int cont = 0;

        boolean goodSentenceFound = false;
        String leftOrRight = "";
        int counter = 0;
        do {
            //creating a random sentence using numbers
            for (int i = 0; i < sentenceLength; i++) {
                int test = rand.nextInt(5);
                if (test == 1) {
                    input[i] = rand.nextInt((int) (wordRepository.count() - 1)); //all words
                } else if (test == 2) {
                    input[i] = rand.nextInt((int) (200 - 1)); //common words
                } else {
                    input[i] = rand.nextInt((int) (100 - 1)); //more common words
                }
            }

            ann.setInput(input);
            ann.calculate();
            double[] networkOutputOne = ann.getOutput();

            System.out.println("Generating random senteence " + counter++);

            if (networkOutputOne[0] == 1d) {
                leftOrRight += "left ";
                goodSentenceFound = true;
            }
            if (networkOutputOne[1] == 1d) {
                leftOrRight += "right";
                goodSentenceFound = true;
            }
            if (networkOutputOne[0] == 0d && networkOutputOne[1] == 0d) {
                leftOrRight += "neutral";
                goodSentenceFound = true;
            }
        } while (!goodSentenceFound);

        String sentenceWords = "";
        for (int i = 0; i < sentenceLength; i++) {
            Word word = wordRepository.getByValue(input[i]);
            if (word != null) {
                sentenceWords += word.getName() + " ";
            }
        }

        return sentenceWords;
    }

    public String calculateLeftOrRight(String tweet) throws IOException {
        Training training = trainingRepository.getByStatus("trained!");

        File file = new File("Net.nnet");

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(training.getAiFile()); //write files into byte
        fos.close();

        NeuralNetwork ann = NeuralNetwork.createFromFile("Net.nnet");

        //fill info here
        double input[] = new double[aIInputSize];
        String[] words = tweet.split(" ");
        int cont = 0;
        for (String word : words) {
            if (cont >= aIInputSize - 1) {
                break;
            }

            Double numberValue = 0d;
            try {
                numberValue = wordRepository.findByName_IgnoreCase(cleanWord(word)).get(0).getValue();
            } catch (Exception ex) {
                System.out.println("Word not found, skipping: " + word);
            }

            if (numberValue == null) {
                numberValue = 0d;
            }

            input[cont] = numberValue;
            cont++;
        }

        ann.setInput(input);
        ann.calculate();
        double[] networkOutputOne = ann.getOutput();

        String leftOrRight = "";
        if (networkOutputOne[0] != 0d) {
            leftOrRight += "left ";
        } else if (networkOutputOne[1] != 0d) {
            leftOrRight += "right";
        } else {
            leftOrRight = "neutral";
        }

        String resultado = "Result : " + leftOrRight;

        System.out.println(resultado);

        return leftOrRight;
    }

    /**
     * {@code PUT  /trainings/:id} : Updates an existing training.
     *
     * @param id the id of the training to save.
     * @param training the training to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated training,
     * or with status {@code 400 (Bad Request)} if the training is not valid,
     * or with status {@code 500 (Internal Server Error)} if the training couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/trainings/{id}")
    public ResponseEntity<Training> updateTraining(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Training training
    ) throws URISyntaxException {
        log.debug("REST request to update Training : {}, {}", id, training);
        if (training.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, training.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!trainingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Training result = trainingRepository.save(training);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, training.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /trainings/:id} : Partial updates given fields of an existing training, field will ignore if it is null
     *
     * @param id the id of the training to save.
     * @param training the training to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated training,
     * or with status {@code 400 (Bad Request)} if the training is not valid,
     * or with status {@code 404 (Not Found)} if the training is not found,
     * or with status {@code 500 (Internal Server Error)} if the training couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/trainings/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Training> partialUpdateTraining(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Training training
    ) throws URISyntaxException {
        log.debug("REST request to partial update Training partially : {}, {}", id, training);
        if (training.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, training.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!trainingRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Training> result = trainingRepository
            .findById(training.getId())
            .map(existingTraining -> {
                if (training.getTwitterFeedFile() != null) {
                    existingTraining.setTwitterFeedFile(training.getTwitterFeedFile());
                }
                if (training.getTwitterFeedFileContentType() != null) {
                    existingTraining.setTwitterFeedFileContentType(training.getTwitterFeedFileContentType());
                }
                if (training.getAiFile() != null) {
                    existingTraining.setAiFile(training.getAiFile());
                }
                if (training.getAiFileContentType() != null) {
                    existingTraining.setAiFileContentType(training.getAiFileContentType());
                }
                if (training.getTimeStamp() != null) {
                    existingTraining.setTimeStamp(training.getTimeStamp());
                }
                if (training.getStatus() != null) {
                    existingTraining.setStatus(training.getStatus());
                }
                if (training.getIsLeft() != null) {
                    existingTraining.setIsLeft(training.getIsLeft());
                }

                return existingTraining;
            })
            .map(trainingRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, training.getId().toString())
        );
    }

    /**
     * {@code GET  /trainings} : get all the trainings.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of trainings in body.
     */
    @GetMapping("/trainings")
    public ResponseEntity<List<Training>> getAllTrainings(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Trainings");
        Page<Training> page = trainingRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /trainings/:id} : get the "id" training.
     *
     * @param id the id of the training to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the training, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/trainings/{id}")
    public ResponseEntity<Training> getTraining(@PathVariable Long id) {
        log.debug("REST request to get Training : {}", id);
        Optional<Training> training = trainingRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(training);
    }

    /**
     * {@code DELETE  /trainings/:id} : delete the "id" training.
     *
     * @param id the id of the training to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/trainings/{id}")
    public ResponseEntity<Void> deleteTraining(@PathVariable Long id) {
        log.debug("REST request to delete Training : {}", id);
        trainingRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
