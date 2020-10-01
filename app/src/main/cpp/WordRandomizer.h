#pragma once
#include <random>
#include <stdexcept>
#include <string>
#include <vector>
#include <unordered_map>


class WordRandomizer final
{
public:
    static std::vector<std::string> getRandomWords(const std::vector<std::string>& words, size_t n);
    //unlike std::shuffle uses all words from input to shuffle n first words;
    template <typename T>
    static void shuffleNFirstWords(T itBegin, T itMiddle, T itEnd)
    {
        std::random_device rd;
        std::mt19937 g(rd());
        if (itMiddle == itEnd) std::shuffle(itBegin, itMiddle, g);
        else
        {
            for (auto it = itBegin; it != itMiddle; ++it)
            {
                auto j = g() % std::distance(it, itEnd);
                std::iter_swap(it, std::next(it, j));
            }
        }
    }
};
