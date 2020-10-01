#include <jni.h>
#include <vector>

#include "CrosswordBuilder.h"
//TODO think about exceptions


extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_crosswordToLearn_CrosswordBuilderWrapper_getCrossword(JNIEnv *env, jobject,
        jobject words, jint wordCount, jint maxSideSize, jint maxTime){
    try
    {
        if(wordCount < 2 )
        {
            throw std::runtime_error{"The amount of word should be more or equal to two."};
        }
        if(maxSideSize < 2 )
        {
            throw std::runtime_error{"The maximum length"
                                     " of a side of the board should be more or equal to two."};
        }
        auto arrayListClass = env->FindClass("java/util/ArrayList");
        if (arrayListClass == nullptr) return nullptr;
        auto getArrayList = env->GetMethodID(arrayListClass,
                "get", "(I)Ljava/lang/Object;");
        auto sizeArrayList = env->GetMethodID(arrayListClass, "size", "()I");
        auto size = static_cast<std::size_t>(env->CallIntMethod(words, sizeArrayList));
        if(wordCount > size){
            throw std::runtime_error{"It is impossible to get more words than it was passed."};
        }
        std::vector<std::string> inp;
        inp.reserve(size);
        for (int i = 0; i < size; ++i) {
            auto rv = static_cast<jstring>(env->CallObjectMethod(words, getArrayList, i));
            const auto strReturn = env->GetStringUTFChars(rv, nullptr);
            inp.emplace_back(strReturn);
        }
        auto res = CrosswordBuilder::build(inp, wordCount, maxSideSize,
                std::chrono::milliseconds(maxTime));
        if(!res) return nullptr;
        auto cross = res->getCrossword();
        auto wordParamsClass = env->FindClass("com/example/crosswordToLearn/WordParams");
        auto constructorWordParams = env->GetMethodID(wordParamsClass, "<init>", "()V");
        auto crosswordParamsClass =
                env->FindClass("com/example/crosswordToLearn/CrosswordParams");
        auto constructorCrosswordParams =
                env->GetMethodID(crosswordParamsClass, "<init>", "()V");
        auto out = env->NewObject(crosswordParamsClass, constructorCrosswordParams);
        auto crosswordParamsWidth = env->GetFieldID(crosswordParamsClass, "width", "I");
        env->SetIntField(out, crosswordParamsWidth, static_cast<jint>(cross.width));
        auto crosswordParamsHeight= env->GetFieldID(crosswordParamsClass, "height", "I");
        env->SetIntField(out, crosswordParamsHeight, static_cast<jint>(cross.height));
        auto crosswordParamsAddWord =
                env->GetMethodID(crosswordParamsClass,
                        "addWord",
                        "(Lcom/example/crosswordToLearn/WordParams;)Z");
        auto xField = env->GetFieldID(wordParamsClass, "x", "I");
        auto yField = env->GetFieldID(wordParamsClass, "y", "I");
        auto wordField = env->GetFieldID(wordParamsClass, "word", "Ljava/lang/String;");
        auto isHorizontalField = env->GetFieldID(wordParamsClass, "isHorizontal", "Z");
        for (const auto& p : cross.words)
        {
            auto params = env->NewObject(wordParamsClass, constructorWordParams);
            jstring word = env->NewStringUTF(p.word.c_str());
            env->SetObjectField(params, wordField, word);
            env->SetIntField(params, xField, static_cast<jint >(p.params.start.x));
            env->SetIntField(params, yField, static_cast<jint >(p.params.start.y));
            auto isHorizontal = p.params.orientation == Utils::WordOrientation::HORIZONTAL;
            env->SetBooleanField(params, isHorizontalField, static_cast<jboolean>(isHorizontal));
            env->CallBooleanMethod(out, crosswordParamsAddWord, params);
            env->DeleteLocalRef(word);
            env->DeleteLocalRef(params);
        }

        return out;
    }
    catch(...)
    {
        return nullptr;
    }
}
