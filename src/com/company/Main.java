package com.company;

import java.io.*;
import java.lang.String;
import java.util.*;

public class Main
{
    final static String ITEM_SPLIT = " ";// ayırıcı
    private static LinkedHashSet<String> itemList;
    
    public static void main(String[] args) throws IOException
    {
        Main main = new Main(); // instance fonksiyonları kullanmak için.
        System.out.println("Veri setinin adresini girin => (örn: D:\\Downloaded\\testDataset.txt)");
        Scanner scan = new Scanner(System.in); // kullanıcıdan yolu alıyoruz.
        String path = scan.next();
    
        System.out.println("Minimum destek değerini girin => ");
        int minSupport = scan.nextInt();
    
        Map<String, Integer> tempFrequentSetMap;
        LinkedHashMap<String, Integer> frequentSetMap = new LinkedHashMap<>(); // LinkedHashMap verileri sıralar. HashMap default getirir.
        ArrayList<String[]> listFromFile = main.ReadDataSet(path);
        
        tempFrequentSetMap = main.FindFrequentOneSets(listFromFile, minSupport); // geçici olarak veri setleri tutulur.
        
        while(!tempFrequentSetMap.isEmpty())
        {
            frequentSetMap.putAll(tempFrequentSetMap);
            tempFrequentSetMap = main.GetCandidateSetMap(tempFrequentSetMap); // inputMap önce 1 liler sonra ikililer bu şekilde güncellenip döner.
            tempFrequentSetMap = main.GetFrequentSetMap(listFromFile, tempFrequentSetMap, minSupport);
        }
        
        List<String> list = new ArrayList<>(frequentSetMap.keySet());
        //Collections.sort(list); // liste 1,12,123,1235,13,135 gibi sıralı, bunu 1,12,13,15,23,25,35 gibi bulmalıyız.
        
        Exporter(list, frequentSetMap);
        System.out.println("JAVA Apriori frequentItemsets.txt içerisine yazıldı..");
    }
    
    
    
    public ArrayList<String[]> ReadDataSet(String path) throws IOException // .txt dosyasını okuyup apriori algoritmasını uygulamak için return ediyoruz.
    {
        String data = "";
        itemList = new LinkedHashSet<>(); // veriyi Set<itemList> aktardık.
        ArrayList<String[]> arrayList = new ArrayList<>();
        
        FileReader fileReader = new FileReader(path); // yolu okuduk.
        BufferedReader reader = null;
        
        reader = new BufferedReader(fileReader);
        try
        {
            while((data = reader.readLine()) != null)
            {
                String[] columns = data.split(ITEM_SPLIT);  // itemleri temizledik.
                arrayList.add(columns);
                for (String column : columns)
                {
                    itemList.add(column); // itemList set olduğu için aynı verileri tekrar içeri almayacak.
                }
            }
            reader.close(); // reader kapatır.
            return arrayList;
        }
        
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                 catch (IOException e1){ }
            }
        }
        return null;
    }
    
    
    
    
    private Map<String, Integer> FindFrequentOneSets(ArrayList<String[]> dataList, int minSupport) // bu metotla tekli itemsetleri hesaplayacağız. 1,2,3,4,5;
    {
        Map<String, Integer> frequentOneSet = new HashMap<>(); // değeri supportla eşlemek için map kullandık.
        
        for (String item: itemList)
        {
            frequentOneSet.put(item, 0); // itemleri 0 support değeri ile aktar.
        }
        for (String[] sample : dataList) // sample ile diziyi tuttuk.
        {
            for (String column : sample ) // dizinin içindeki string[index] değerleri geziyoruz.
            {
                frequentOneSet.put(column, frequentOneSet.get(column)+1); // column değeri görürsen value(support) artır.
            }
        }
        Iterator<Map.Entry<String, Integer>> iterator = frequentOneSet.entrySet().iterator();
        
        while(iterator.hasNext())
        {
            Map.Entry<String, Integer> entry = iterator.next(); // map sırasıyla gezilir.
            if (entry.getValue() < minSupport) // minSupport tan küçük ise silinir.
            {
                iterator.remove();
            }
        }
        return frequentOneSet;
    }
    
    
    
    private LinkedHashMap<String, Integer> GetCandidateSetMap(Map<String, Integer> inputMap) // itemsetler bulunacak. GetFrequentSetMap te ise support değerleri hesaplanacak.
    {
        Set<String> keyItems = new LinkedHashSet<>();  // keylerle 1,2,3,4,5 oluşturulan itemsetler.
        LinkedHashMap<String, Integer> results = new LinkedHashMap<>();
        Set<String> keys = inputMap.keySet(); // 1,2,3,4,5 support değerine bağlı 4 olmayabilir.
        int keySetLength = keys.iterator().next().split(ITEM_SPLIT).length+1; // virgül ile 4 +1
        
        for (String key: keys)
        {
            for (String key2 : keys)
            {
                if (key.equals(key2))  // 11 22 gibi istemediğimiz sonuçlar engellendi.
                {
                    continue;
                }
                // key 1 iken key2 2,3,4,5 gibi sonuçlar elde ediliyor.
                String[] list1 = key.split(ITEM_SPLIT);
                String[] list2 = key2.split(ITEM_SPLIT);
                Set<String> l3 = new HashSet<>();
                
                // key=2 iken 2,3,4,5-2,4,5-2,5 gibi
                // setleri buluyor
                for (String l1 : list1)
                {
                    l3.add(l1); // key = 1 olsun.
                }
                for (String l2 : list2)
                {
                    l3.add(l2); // key2 = 2 olsun. sonra 1,2-1,2,3-1,2,3,4 gibi işlemlerle itemseti hesaplıyoruz.
                }
                
                if (l3.size() != keySetLength) // eleman tükenmediyse iterasyona devam et.
                {
                    continue;
                }
                
                List<String> l3List = new ArrayList<>(l3.size());
                for (String ll3 : l3)  // l3 tekrarlı veriler içermedi. l3Liste böylece aktardık.
                {
                    l3List.add(ll3);
                }
                Collections.sort(l3List); // l3List sıralandı
                
                String[] l3Array = new String[l3List.size()];
                l3List.toArray(l3Array);    // l3List i diziye aktardık.
                keyItems.add(String.join(ITEM_SPLIT, l3Array));
            }
        }
        for (String string : keyItems)
        {
            results.put(string, 0); // string-key, 0-value(support) değerlerini aktardık.
        }
        return results;
    }
    
    
    private Map<String, Integer> GetFrequentSetMap(ArrayList<String[]> inputList, Map<String, Integer> inputMap, int minSupport)
    {   // bulunan ıtemsetlerin support değerleri hesaplanacak. Bu metodun amacı bu.
        
        int flag = 0;
        List<String> list = new ArrayList<>();
        Set<String> keySet = new LinkedHashSet<>();
        keySet.addAll(inputMap.keySet()); // itemSetleri keyset e aktardık.
    
        for (String[] strings : inputList)
        {
            for (int i=0; i< strings.length; i++)
            {
                list.add(strings[i]);
            }
        
            for (String string: keySet)  // keySet iterate edilir.
            {
                String[] keyItem = string.split(ITEM_SPLIT); // keySetteki veriyi keyItem içine aldık.
                flag = keyItem.length;
            
                for (String string2 : keyItem)
                {
                    if (list.contains(string2))
                    {
                        --flag; // bu support counter için var.
                    }
                }
            
                if (flag == 0)
                {
                    inputMap.put(string, inputMap.get(string) + 1); // flag 0 sa support artır.
                }
            }
            list.clear();
        }
    
        for (String string : keySet)
        {
            if (inputMap.get(string)< minSupport) // eğer minimum supporttan küçükse ele.
            {
                inputMap.remove(string);
            }
        }
    
        return inputMap;
    }
    
    
    
    public static void Exporter(List<String> list, LinkedHashMap<String, Integer> frequentMap) throws FileNotFoundException
    {
        try (PrintWriter exporter = new PrintWriter("frequentItemsets.txt")) // yazılacak dosya. Project_Apriori klasörü içine.
        {
            for (String string: list)
            {
                exporter.println(string + " :" + frequentMap.get(string));
                // string ile frequentMap key ini okuyup, get metoduylada o key in valuesunu yazdırıyoruz.
            }
        }
    }
}







