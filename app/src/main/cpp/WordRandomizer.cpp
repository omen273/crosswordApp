#include "WordRandomizer.h"

#include <random>
#include <stdexcept>
#include <unordered_map>

std::vector<std::string> WordRandomizer::getRandomWords(const std::vector<std::string>& words,
        size_t n)
{
    if (n > words.size() - 1) [[unlikely]]
    {
        throw std::runtime_error{ "The output words' number should be"
                                  " more or equal to the input words' number minus one" };
    }
    std::random_device rd;
    std::mt19937 g(rd());
    std::unordered_map<std::size_t, std::size_t> buffer;
    std::vector<std::string> res;
    res.reserve(n);
    buffer.reserve(n);
    for (std::size_t i = 0; i < n; ++i)
    {

        auto itI = buffer.find(i);
        auto j = g() % (words.size() - i) + i;
        auto itJ = buffer.find(j);
        if (itI == buffer.end() && itJ == buffer.end())
        {
            res.push_back(words[j]);
            if (i != j) buffer.emplace(j, i);
        }
        else if (itI == buffer.end() && itJ != buffer.end())
        {
            res.push_back(words[buffer[j]]);
            buffer[j] = i;
        }
        else if (itI != buffer.end() && itJ == buffer.end())
        {
            res.push_back(words[j]);
            buffer.emplace(j, buffer[i]);
            buffer.erase(i);
        }
        else
        {
            res.push_back(words[buffer[j]]);
            buffer[j] = buffer[i];
            buffer.erase(i);
        }
    }

    return res;
}
